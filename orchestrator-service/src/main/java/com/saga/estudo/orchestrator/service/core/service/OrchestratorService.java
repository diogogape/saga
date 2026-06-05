package com.saga.estudo.orchestrator.service.core.service;

import com.saga.estudo.orchestrator.service.core.dto.Event;
import com.saga.estudo.orchestrator.service.core.dto.History;
import com.saga.estudo.orchestrator.service.core.enums.EEventSource;
import com.saga.estudo.orchestrator.service.core.enums.ESagaStatus;
import com.saga.estudo.orchestrator.service.core.enums.ETopics;
import com.saga.estudo.orchestrator.service.core.producer.KafkaProducer;
import com.saga.estudo.orchestrator.service.core.saga.SagaExecutionController;
import com.saga.estudo.orchestrator.service.core.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OrchestratorService {

    @Autowired
    private SagaExecutionController sagaExecutionController;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private JsonUtil util;

    public void startSaga(Event event){
        event.setSource(EEventSource.ORCHESTRATOR);
        event.setStatus(ESagaStatus.SUCCESS);
        log.info("SAGA STARTED");
        ETopics topic  = sagaExecutionController.getNextTopic(event);
        addHistory(event, "Saga started");
        kafkaProducer.sendEvent(topic.getTopic(), util.toJson(event));


    }

    public void finishSagaSuccess(Event event){
        event.setSource(EEventSource.ORCHESTRATOR);
        event.setStatus(ESagaStatus.SUCCESS);
        log.info("SAGA FINISHED WITH SUCCESS");
        addHistory(event, "Saga finish with success");
        notifyEnd(event);
    }

    public void finishSagaFail(Event event){
        event.setSource(EEventSource.ORCHESTRATOR);
        event.setStatus(ESagaStatus.FAIL);
        log.info("SAGA FINISHED WITH FAIL ");
        addHistory(event, "Saga finish with fail");
        notifyEnd(event);
    }

    public void continueSaga(Event event){
        ETopics topic  = sagaExecutionController.getNextTopic(event);
        addHistory(event, "Saga continue");
        kafkaProducer.sendEvent(topic.getTopic(), util.toJson(event));
    }

    private void notifyEnd(Event event){
        kafkaProducer.sendEvent(ETopics.NOTIFY_ENDING.getTopic(), util.toJson(event));
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

}
