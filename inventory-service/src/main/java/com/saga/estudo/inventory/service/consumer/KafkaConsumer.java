package com.saga.estudo.inventory.service.consumer;



import com.saga.estudo.inventory.service.service.InventoryService;
import com.saga.estudo.inventory.service.utils.JsonUtil;
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
    private InventoryService inventoryService;


    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.inventory-success}"
    )
    public void finishSuccessConsumer(String payload){
        log.info("receive event {} from inventory-success topic", payload);
        var event = utils.toEvent(payload);
        inventoryService.updateInventory(event);
    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.inventory-fail}"
    )
    public void finishFailConsumer(String payload){
        log.info("receive event {} from inventory-fail topic", payload);
        var event = utils.toEvent(payload);
        inventoryService.rollbackInvetory(event);
    }



}
