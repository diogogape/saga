package com.saga.estudo.payment.service.service;

import com.saga.estudo.payment.service.config.exception.ValidationException;
import com.saga.estudo.payment.service.dto.Event;
import com.saga.estudo.payment.service.dto.History;
import com.saga.estudo.payment.service.enums.EPaymentStatus;
import com.saga.estudo.payment.service.enums.ESagaStatus;
import com.saga.estudo.payment.service.model.Payment;
import com.saga.estudo.payment.service.producer.KafkaProducer;
import com.saga.estudo.payment.service.repository.PaymentRepository;
import com.saga.estudo.payment.service.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentService {

    private static final String SOURCE ="PAYMENT_SERVICE";
    public static final double MIN_AMOUNT = 0.1;

    @Value("${kafka.topic.orchestrator}")
    private String orchestratorTopic;

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private PaymentRepository paymentRepository;

    public void realizePayment(Event event){
        try {
            event.validate();
            validateExistPayment(event);
            createPendingPayment(event);
            Payment payment = findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId());
            validatePayment(payment);
            changePaymentToSuccess(payment);
            handleSuccess(event);
        } catch (Exception e) {
            log.error("Error trying payment", e);
            handleFailCurrentPayment(event, e.getMessage());
        }
        kafkaProducer.sendEvent(orchestratorTopic, jsonUtil.toJson(event));
    }

    public void rollbackPayment(Event event){
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(SOURCE);
        try{
            changePaymentToRefund(event);
            addHistory(event,"Rollback payment ok ");
        } catch (Exception e) {
            addHistory(event,"Fail on rollback payment: ".concat(e.getMessage()));
        }
        kafkaProducer.sendEvent(orchestratorTopic, jsonUtil.toJson(event));
    }

    private void changePaymentToRefund(Event event) {
        Payment payment = findByOrderIdAndTransactionId(event.getOrderId(),event.getTransactionId());
        payment.setStatus(EPaymentStatus.REFUND);
        paymentRepository.save(payment);
    }

    private void handleFailCurrentPayment(Event event, String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(SOURCE);
        addHistory(event,"Fail payment: ".concat(message));
    }

    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(SOURCE);
        addHistory(event, "Payment successfully");
    }

    private void addHistory(Event event, String message) {
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void changePaymentToSuccess(Payment payment) {
        payment.setStatus(EPaymentStatus.SUCCESS);
        paymentRepository.save(payment);
    }

    private void validatePayment(Payment payment) {
        if (payment.getTotalAmount() < MIN_AMOUNT){
            throw  new ValidationException("Total Amount Invalid");
        }
    }

    private Payment findByOrderIdAndTransactionId(String orderId, String transactionId){
        return paymentRepository.findByOrderIdAndTransactionId(orderId, transactionId)
                .orElseThrow(()-> new ValidationException("Payment not found"));
    }

    private void createPendingPayment(Event event) {
        Payment payment = Payment.builder()
                .totalItems(event.calculeteTotalItems())
                .totalAmount(event.calculateAmont())
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .build();
        paymentRepository.save(payment);


    }

    private void validateExistPayment(Event event) {
        if (paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(),event.getTransactionId())){
            throw new ValidationException("There is another transactionId for this validation");
        }
    }

}
