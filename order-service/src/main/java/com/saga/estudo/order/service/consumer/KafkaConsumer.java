package com.saga.estudo.order.service.consumer;

import com.saga.estudo.order.service.document.Event;
import com.saga.estudo.order.service.service.EventService;
import com.saga.estudo.order.service.utils.JsonUtil;
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
    private EventService eventService;


    @KafkaListener(
        groupId ="${spring.kafka.consumer.group-id}",
        topics ="${kafka.topic.notify-ending}"
    )
    public void endingConsumer(String payload){
        log.info("receive event {} from notify-ending topic", payload);
        Event event = utils.toEvent(payload);
        eventService.notifyEnd(event);
    }

}
