package com.saga.estudo.payment.service.repository;

import com.saga.estudo.payment.service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Integer> {

    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
