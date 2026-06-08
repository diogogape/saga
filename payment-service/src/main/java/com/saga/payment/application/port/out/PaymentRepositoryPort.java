package com.saga.payment.application.port.out;


import com.saga.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepositoryPort {
    boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);
    Payment save(Payment payment);
}
