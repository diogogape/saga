package com.saga.payment.infrastructure.adapter.out.persistence;

import com.saga.payment.application.port.out.PaymentRepositoryPort;
import com.saga.payment.domain.exception.ValidationException;
import com.saga.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private  final PaymentRepository paymentRepository;

    @Override
    public boolean existsByOrderIdAndTransactionId(String orderId, String transactionId) {
        return paymentRepository.existsByOrderIdAndTransactionId(orderId,transactionId);
    }

    @Override
    public Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId) {
        return paymentRepository.findByOrderIdAndTransactionId(orderId,transactionId)
                .map(entity->{
                    Payment payment = new Payment();
                    BeanUtils.copyProperties(entity, payment);
                    return payment;
                });
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        BeanUtils.copyProperties(payment,entity);
        BeanUtils.copyProperties(paymentRepository.save(entity),payment);
        return payment;
    }
}
