package com.saga.payment.infrastructure.adapter.out.messaging;

import com.saga.payment.application.port.out.EventPublisherPort;
import com.saga.payment.domain.model.Event;
import com.saga.payment.infrastructure.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer implements EventPublisherPort {


    private final KafkaTemplate<String,String> kafkaTemplate;

    @Value("${kafka.topic.orchestrator}")
    private String orchestratorTopic;

    private final JsonUtil jsonUtil;

    public void sendEvent(Event event){
        var payload = jsonUtil.toJson(event);
        try {
            this.kafkaTemplate.send(orchestratorTopic, payload);
            log.info("Sending event to topic {} and data {} ", orchestratorTopic, payload);
        } catch (Exception e) {
            log.error("Error to send topic {} and data {}", orchestratorTopic, payload, e);
        }
    }

}
