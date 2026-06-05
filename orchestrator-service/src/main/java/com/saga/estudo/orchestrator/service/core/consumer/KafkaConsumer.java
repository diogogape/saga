package com.saga.estudo.orchestrator.service.core.consumer;


import com.saga.estudo.orchestrator.service.core.service.OrchestratorService;
import com.saga.estudo.orchestrator.service.core.utils.JsonUtil;
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
    private OrchestratorService orchestratorService;


    @KafkaListener(
        groupId ="${spring.kafka.consumer.group-id}",
        topics ="${kafka.topic.start-saga}"
    )
    public void statSagaConsumer(String payload){
        log.info("receive event {} from start-saga topic", payload);
        var event = utils.toEvent(payload);
        orchestratorService.startSaga(event);


    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.finish-success}"
    )
    public void finishSuccessConsumer(String payload){
        log.info("receive event {} from finish-success topic", payload);
        var event = utils.toEvent(payload);
        orchestratorService.finishSagaSuccess(event);


    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.finish-fail}"
    )
    public void finishFailConsumer(String payload){
        log.info("receive event {} from finish-fail topic", payload);
        var event = utils.toEvent(payload);
        orchestratorService.finishSagaFail(event);

    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.orchestrator}"
    )
    public void orchestratorConsumer(String payload){
        log.info("receive event {} from orchestrator topic", payload);
        var event = utils.toEvent(payload);
        orchestratorService.continueSaga(event);
    }

}
