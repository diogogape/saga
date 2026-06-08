package com.saga.payment.domain.model;

import com.saga.payment.domain.enums.ESagaStatus;
import com.saga.payment.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Event (domínio)")
class EventTest {

    private Order validOrder() {
        Product product = Product.builder().code("A").unitValue(10.0).build();
        OrderProduct orderProduct = OrderProduct.builder().product(product).quantity(2L).build();
        return Order.builder().id("order-1").products(List.of(orderProduct)).build();
    }

    private Event validEvent() {
        return Event.builder()
                .id("evt-1")
                .transactionId("tx-1")
                .orderId("order-1")
                .payload(validOrder())
                .build();
    }

    @Test
    @DisplayName("validate aceita evento completo")
    void validate_shouldPassForValidEvent() {
        validEvent().validate(); // não deve lançar
    }

    @Test
    @DisplayName("validate lança exceção quando faltam dados obrigatórios")
    void validate_shouldFailWhenMissingData() {
        Event event = Event.builder().payload(validOrder()).build(); // sem transactionId/orderId

        assertThatThrownBy(event::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessage("TransactionId and OrderId and Order must be informed.");
    }

    @Test
    @DisplayName("calculateAmount e calculateTotalItems delegam para o payload")
    void calculate_shouldDelegateToPayload() {
        Event event = validEvent();

        assertThat(event.calculateAmount()).isEqualTo(20.0);
        assertThat(event.calculateTotalItems()).isEqualTo(2L);
    }

    @Test
    @DisplayName("addToHistory inicializa a lista e registra a entrada")
    void addToHistory_shouldCreateAndAppend() {
        Event event = validEvent();
        event.setSource("PAYMENT_SERVICE");
        event.setStatus(ESagaStatus.SUCCESS);

        event.addToHistory("primeira mensagem");

        assertThat(event.getEventHistory()).hasSize(1);
        History history = event.getEventHistory().get(0);
        assertThat(history.getMessage()).isEqualTo("primeira mensagem");
        assertThat(history.getSource()).isEqualTo("PAYMENT_SERVICE");
        assertThat(history.getStatus()).isEqualTo(ESagaStatus.SUCCESS);
        assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateEvent altera source, status e adiciona histórico")
    void updateEvent_shouldMutateAndRecordHistory() {
        Event event = validEvent();

        event.updateEvent("PAYMENT_SERVICE", ESagaStatus.ROLLBACK_PENDING, "falhou");

        assertThat(event.getSource()).isEqualTo("PAYMENT_SERVICE");
        assertThat(event.getStatus()).isEqualTo(ESagaStatus.ROLLBACK_PENDING);
        assertThat(event.getEventHistory()).hasSize(1);
        assertThat(event.getEventHistory().get(0).getMessage()).isEqualTo("falhou");
        assertThat(event.getEventHistory().get(0).getStatus()).isEqualTo(ESagaStatus.ROLLBACK_PENDING);
    }
}
