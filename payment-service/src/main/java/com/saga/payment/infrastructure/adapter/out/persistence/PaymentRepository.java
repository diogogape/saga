package com.saga.payment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity,Integer> {

    boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    Optional<PaymentEntity> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
