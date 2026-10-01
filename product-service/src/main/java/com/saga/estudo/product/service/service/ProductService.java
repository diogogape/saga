package com.saga.estudo.product.service.service;

import com.saga.estudo.product.service.config.exception.ValidationException;
import com.saga.estudo.product.service.dto.Event;
import com.saga.estudo.product.service.dto.History;
import com.saga.estudo.product.service.enums.ESagaStatus;
import com.saga.estudo.product.service.model.Validation;
import com.saga.estudo.product.service.producer.KafkaProducer;
import com.saga.estudo.product.service.repository.ProductRepository;
import com.saga.estudo.product.service.repository.ValidationRepository;
import com.saga.estudo.product.service.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ProductService {

    private static final String SOURCE ="PRODUCT_VALIDATION_SERVICE";

    @Value("${kafka.topic.orchestrator}")
    private String orchestratorTopic;

    @Autowired
    private  JsonUtil jsonUtil;
    @Autowired
    private  KafkaProducer kafkaProducer;
    @Autowired
    private  ProductRepository productRepository;
    @Autowired
    private  ValidationRepository validationRepository;


    public void validateProducts(Event event){
        try {
            event.validate();
            validateExistValidation(event);
            validateExistProduct(event);
            createValidation(event, true);
            handleSuccess(event);
        } catch (Exception e) {
            log.error("Error trying validate products", e);
            handleFailCurrentNotExecuted(event,e.getMessage());
        }
        kafkaProducer.sendEvent(orchestratorTopic, jsonUtil.toJson(event));
    }

    public void handleRollback(Event event){
        changeValidationToFail(event);
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(SOURCE);
        addHistory(event, "Rollback executed on product validation");
        kafkaProducer.sendEvent(orchestratorTopic, jsonUtil.toJson(event));
    }

    private void changeValidationToFail(Event event) {
        validationRepository.findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                .ifPresentOrElse(validation -> {
                    validation.setSuccess(false);
                    validationRepository.save(validation);
                    }, ()->createValidation(event,false)
                );
    }

    @Transactional(readOnly = true)
    private void validateExistValidation(Event event) {
        if (validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(),event.getTransactionId())){
            throw new ValidationException("There is another transactionId for this validation");
        }
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(SOURCE);
        addHistory(event,"Fail to validate produts: ".concat(message));
    }

    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(SOURCE);
        addHistory(event, "Products validated successfully");
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

    @Transactional
    private void createValidation(Event event, boolean success) {
        Validation validation = Validation.builder()
                .transactionId(event.getTransactionId())
                .orderId(event.getOrderId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }

    @Transactional(readOnly = true)
    private void validateExistProduct(Event event) {
        event.getPayload().getProducts().forEach(p ->{
            if(!productRepository.existsByCode(p.getProduct().getCode())){
                throw new ValidationException("Product not exist on database.");
            }
        });
    }


}
