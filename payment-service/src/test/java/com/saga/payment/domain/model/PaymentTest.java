package com.saga.payment.domain.model;

import com.saga.payment.domain.enums.EPaymentStatus;
import com.saga.payment.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment (domínio)")
class PaymentTest {

    @Test
    @DisplayName("createPending preenche os dados e nasce com status PENDING")
    void createPending_shouldStartAsPending() {
        Payment payment = new Payment();

        payment.createPending(3L, 99.9, "order-1", "tx-1");

        assertThat(payment.getTotalItems()).isEqualTo(3L);
        assertThat(payment.getTotalAmount()).isEqualTo(99.9);
        assertThat(payment.getOrderId()).isEqualTo("order-1");
        assertThat(payment.getTransactionId()).isEqualTo("tx-1");
        assertThat(payment.getStatus()).isEqualTo(EPaymentStatus.PENDING);
    }

    @Test
    @DisplayName("validatePayment com valor válido transita para SUCCESS")
    void validatePayment_shouldConfirmWhenAmountIsValid() {
        Payment payment = new Payment();
        payment.createPending(1L, Payment.MIN_AMOUNT, "order-1", "tx-1");

        payment.validatePayment();

        assertThat(payment.getStatus()).isEqualTo(EPaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("validatePayment abaixo do mínimo lança exceção e não confirma")
    void validatePayment_shouldFailWhenAmountBelowMinimum() {
        Payment payment = new Payment();
        payment.createPending(1L, 0.0, "order-1", "tx-1");

        assertThatThrownBy(payment::validatePayment)
                .isInstanceOf(ValidationException.class)
                .hasMessage("Total Amount Invalid");

        assertThat(payment.getStatus()).isEqualTo(EPaymentStatus.PENDING);
    }

    @Test
    @DisplayName("refund transita para REFUND")
    void refund_shouldChangeStatusToRefund() {
        Payment payment = new Payment();
        payment.createPending(1L, 10.0, "order-1", "tx-1");

        payment.refund();

        assertThat(payment.getStatus()).isEqualTo(EPaymentStatus.REFUND);
    }
}
