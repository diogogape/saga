package com.saga.estudo.orchestrator.service.core.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void sendEvent(String topic, String payload){
        try {
            this.kafkaTemplate.send(topic, payload);
            log.info("Sending event to topic {} and data {} ", topic, payload);
        } catch (Exception e) {
            log.error("Error to send topic {} and data {}", topic, payload, e);
        }
    }

}
