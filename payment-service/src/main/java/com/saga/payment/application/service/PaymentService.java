package com.saga.payment.application.service;

import com.saga.payment.application.port.in.ProcessPaymentUseCase;
import com.saga.payment.application.port.in.RollbackPaymentUseCase;
import com.saga.payment.application.port.out.EventPublisherPort;
import com.saga.payment.application.port.out.PaymentRepositoryPort;
import com.saga.payment.domain.exception.ValidationException;
import com.saga.payment.domain.model.Event;
import com.saga.payment.domain.model.Payment;
import com.saga.payment.domain.enums.ESagaStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements ProcessPaymentUseCase, RollbackPaymentUseCase {

    private static final String SOURCE ="PAYMENT_SERVICE";


    private final PaymentRepositoryPort repository;   // porta
    private final EventPublisherPort publisher;   // port




    public void processPayment(Event event){
        try {
            event.validate();
            validateExistPayment(event);
            Payment payment = createPendingPayment(event);
            validatePayment(payment);
            handleSuccess(event);
        } catch (Exception e) {
            log.error("Error trying payment", e);
            handleFailCurrentPayment(event, e.getMessage());
        }
        publisher.sendEvent(event);
    }

    public void rollbackPayment(Event event){
        try{
            changePaymentToRefund(event);
            event.updateEvent(SOURCE,ESagaStatus.FAIL,"Rollback payment ok ");
        } catch (Exception e) {
            event.updateEvent(SOURCE,ESagaStatus.FAIL,"Fail on rollback payment: ".concat(e.getMessage()));
        }
        publisher.sendEvent(event);
    }

    private void changePaymentToRefund(Event event) {
        Payment payment = findByOrderIdAndTransactionId(event.getOrderId(),event.getTransactionId());
        payment.refund();
        repository.save(payment);
    }

    private void handleFailCurrentPayment(Event event, String message) {
        event.updateEvent(SOURCE,ESagaStatus.ROLLBACK_PENDING,"Fail payment: ".concat(message));
    }

    private void handleSuccess(Event event) {
        event.updateEvent(SOURCE,ESagaStatus.SUCCESS,"Payment successfully");
    }




    private void validatePayment(Payment payment) {
        payment.validatePayment();
        repository.save(payment);
    }

    private Payment findByOrderIdAndTransactionId(String orderId, String transactionId){
        return repository.findByOrderIdAndTransactionId(orderId, transactionId)
                .orElseThrow(()-> new ValidationException("Payment not found"));
    }

    private Payment createPendingPayment(Event event) {
        Payment payment = new Payment();
        payment.createPending(
                event.calculateTotalItems(),
                event.calculateAmount(),
                event.getOrderId(),
                event.getTransactionId());
        return repository.save(payment);
    }

    private void validateExistPayment(Event event) {
        if (repository.existsByOrderIdAndTransactionId(event.getOrderId(),event.getTransactionId())){
            throw new ValidationException("There is another transactionId for this validation");
        }
    }

}
