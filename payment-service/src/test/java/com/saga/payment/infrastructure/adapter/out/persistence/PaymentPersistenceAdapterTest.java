package com.saga.payment.infrastructure.adapter.out.persistence;

import com.saga.payment.domain.enums.EPaymentStatus;
import com.saga.payment.domain.model.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PaymentPersistenceAdapter.class)
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("PaymentPersistenceAdapter (integração JPA + H2)")
class PaymentPersistenceAdapterTest {

    @Autowired
    private PaymentPersistenceAdapter adapter;

    private Payment pending(String orderId, String txId, long items, double amount) {
        Payment payment = new Payment();
        payment.createPending(items, amount, orderId, txId);
        return payment;
    }

    @Test
    @DisplayName("save gera id e persiste; findBy mapeia de volta para o domínio")
    void save_thenFind_roundTrip() {
        Payment saved = adapter.save(pending("order-1", "tx-1", 3L, 50.0));
        assertThat(saved.getId()).isNotNull();

        Optional<Payment> found = adapter.findByOrderIdAndTransactionId("order-1", "tx-1");

        assertThat(found).isPresent();
        Payment p = found.get();
        assertThat(p.getOrderId()).isEqualTo("order-1");
        assertThat(p.getTransactionId()).isEqualTo("tx-1");
        assertThat(p.getTotalItems()).isEqualTo(3L);
        assertThat(p.getTotalAmount()).isEqualTo(50.0);
        assertThat(p.getStatus()).isEqualTo(EPaymentStatus.PENDING);
    }

    @Test
    @DisplayName("existsByOrderIdAndTransactionId reflete a presença do registro")
    void exists_shouldReflectPresence() {
        assertThat(adapter.existsByOrderIdAndTransactionId("o", "t")).isFalse();

        adapter.save(pending("o", "t", 1L, 10.0));

        assertThat(adapter.existsByOrderIdAndTransactionId("o", "t")).isTrue();
    }

    @Test
    @DisplayName("findBy de registro inexistente retorna Optional.empty")
    void find_absent_shouldReturnEmpty() {
        assertThat(adapter.findByOrderIdAndTransactionId("x", "y")).isEmpty();
    }

    @Test
    @DisplayName("save de pagamento existente atualiza o status (refund)")
    void save_update_shouldPersistRefund() {
        Payment saved = adapter.save(pending("order-2", "tx-2", 1L, 10.0));
        saved.refund();

        adapter.save(saved);

        Optional<Payment> found = adapter.findByOrderIdAndTransactionId("order-2", "tx-2");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(EPaymentStatus.REFUND);
    }
}
