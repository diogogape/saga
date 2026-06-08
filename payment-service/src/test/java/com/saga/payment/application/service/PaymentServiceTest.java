package com.saga.payment.application.service;

import com.saga.payment.application.port.out.EventPublisherPort;
import com.saga.payment.application.port.out.PaymentRepositoryPort;
import com.saga.payment.domain.enums.EPaymentStatus;
import com.saga.payment.domain.enums.ESagaStatus;
import com.saga.payment.domain.model.Event;
import com.saga.payment.domain.model.Order;
import com.saga.payment.domain.model.OrderProduct;
import com.saga.payment.domain.model.Payment;
import com.saga.payment.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService (aplicação)")
class PaymentServiceTest {

    @Mock
    private PaymentRepositoryPort repository;

    @Mock
    private EventPublisherPort publisher;

    @InjectMocks
    private PaymentService paymentService;

    private static final String ORDER_ID = "order-1";
    private static final String TX_ID = "tx-1";

    private Event eventWith(double unitValue, long quantity) {
        Product product = Product.builder().code("A").unitValue(unitValue).build();
        OrderProduct orderProduct = OrderProduct.builder().product(product).quantity(quantity).build();
        Order order = Order.builder().id(ORDER_ID).products(List.of(orderProduct)).build();
        return Event.builder()
                .id("evt-1")
                .transactionId(TX_ID)
                .orderId(ORDER_ID)
                .payload(order)
                .build();
    }

    // ---------- processPayment ----------

    @Test
    @DisplayName("processPayment com sucesso marca evento como SUCCESS e publica")
    void processPayment_success() {
        Event event = eventWith(10.0, 2); // amount = 20.0
        when(repository.existsByOrderIdAndTransactionId(ORDER_ID, TX_ID)).thenReturn(false);
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processPayment(event);

        assertThat(event.getSource()).isEqualTo("PAYMENT_SERVICE");
        assertThat(event.getStatus()).isEqualTo(ESagaStatus.SUCCESS);
        assertThat(event.getEventHistory()).hasSize(1);
        assertThat(event.getEventHistory().get(0).getMessage()).isEqualTo("Payment successfully");
        verify(repository, times(2)).save(any(Payment.class)); // pendente + confirmado
        verify(publisher).sendEvent(event);
    }

    @Test
    @DisplayName("processPayment quando já existe pagamento marca ROLLBACK_PENDING e não persiste")
    void processPayment_whenAlreadyExists() {
        Event event = eventWith(10.0, 2);
        when(repository.existsByOrderIdAndTransactionId(ORDER_ID, TX_ID)).thenReturn(true);

        paymentService.processPayment(event);

        assertThat(event.getStatus()).isEqualTo(ESagaStatus.ROLLBACK_PENDING);
        assertThat(event.getSource()).isEqualTo("PAYMENT_SERVICE");
        assertThat(event.getEventHistory().get(0).getMessage())
                .contains("There is another transactionId");
        verify(repository, never()).save(any(Payment.class));
        verify(publisher).sendEvent(event);
    }

    @Test
    @DisplayName("processPayment com valor abaixo do mínimo marca ROLLBACK_PENDING")
    void processPayment_whenAmountInvalid() {
        Event event = eventWith(0.0, 1); // amount = 0.0 < MIN_AMOUNT
        when(repository.existsByOrderIdAndTransactionId(ORDER_ID, TX_ID)).thenReturn(false);
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processPayment(event);

        assertThat(event.getStatus()).isEqualTo(ESagaStatus.ROLLBACK_PENDING);
        assertThat(event.getEventHistory().get(0).getMessage()).contains("Total Amount Invalid");
        verify(publisher).sendEvent(event);
    }

    // ---------- rollbackPayment ----------

    @Test
    @DisplayName("rollbackPayment realiza refund e marca evento como FAIL")
    void rollbackPayment_success() {
        Event event = eventWith(10.0, 2);
        Payment existing = new Payment();
        existing.createPending(2L, 20.0, ORDER_ID, TX_ID);
        existing.validatePayment(); // SUCCESS
        when(repository.findByOrderIdAndTransactionId(ORDER_ID, TX_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.rollbackPayment(event);

        assertThat(existing.getStatus()).isEqualTo(EPaymentStatus.REFUND);
        assertThat(event.getStatus()).isEqualTo(ESagaStatus.FAIL);
        assertThat(event.getSource()).isEqualTo("PAYMENT_SERVICE");
        verify(repository).save(existing);
        verify(publisher).sendEvent(event);
    }

    @Test
    @DisplayName("rollbackPayment quando pagamento não existe marca FAIL com mensagem de erro")
    void rollbackPayment_whenNotFound() {
        Event event = eventWith(10.0, 2);
        when(repository.findByOrderIdAndTransactionId(ORDER_ID, TX_ID)).thenReturn(Optional.empty());

        paymentService.rollbackPayment(event);

        assertThat(event.getStatus()).isEqualTo(ESagaStatus.FAIL);
        assertThat(event.getEventHistory().get(0).getMessage()).contains("Fail on rollback payment");
        verify(repository, never()).save(any(Payment.class));
        verify(publisher).sendEvent(event);
    }
}
