package com.saga.estudo.order.service.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Value("${kafka.topic.start-saga}")
    private String startSagaTopic;

    public void sendEvent(String payload){
        try {
            this.kafkaTemplate.send(this.startSagaTopic, payload);
            log.info("Sending event to topic {} and data {} ", this.startSagaTopic, payload);
        } catch (Exception e) {
            log.error("Error to send topic {} and data {}", this.startSagaTopic, payload, e);
        }
    }

}
