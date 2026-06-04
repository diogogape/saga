package com.saga.estudo.product.service.consumer;





import com.saga.estudo.product.service.service.ProductService;
import com.saga.estudo.product.service.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private JsonUtil utils;

    @Autowired
    private ProductService productService;


    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.product-validation-success}"
    )
    public void finishSuccessConsumer(String payload){
        log.info("receive event {} from product-validation-success topic", payload);
        var event = utils.toEvent(payload);
        productService.validateProducts(event);


    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.product-validation-fail}"
    )
    public void finishFailConsumer(String payload){
        log.info("receive event {} from product-validation-fail topic", payload);
        var event = utils.toEvent(payload);
        productService.handleRollback(event);


    }



}
