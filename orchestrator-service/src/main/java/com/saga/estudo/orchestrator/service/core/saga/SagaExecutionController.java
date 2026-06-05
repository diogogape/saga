package com.saga.estudo.orchestrator.service.core.saga;

import com.saga.estudo.orchestrator.service.core.dto.Event;
import com.saga.estudo.orchestrator.service.core.enums.EEventSource;
import com.saga.estudo.orchestrator.service.core.enums.ESagaStatus;
import com.saga.estudo.orchestrator.service.core.enums.ETopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.format;

@Slf4j
@Component
public class SagaExecutionController {

    record SagaKey(EEventSource source, ESagaStatus status){}

    public static final Map<SagaKey,ETopics> SAGA_HANDLER = Map.ofEntries(
            Map.entry( new SagaKey(EEventSource.ORCHESTRATOR, ESagaStatus.SUCCESS),ETopics.PRODUCT_VALIDATION_SUCCESS),
            Map.entry( new SagaKey(EEventSource.ORCHESTRATOR, ESagaStatus.FAIL), ETopics.FINISH_FAIL),
            Map.entry( new SagaKey(EEventSource.PRODUCT_VALIDATION_SERVICE, ESagaStatus.SUCCESS), ETopics.PAYMENT_SUCCESS),
            Map.entry( new SagaKey(EEventSource.PRODUCT_VALIDATION_SERVICE, ESagaStatus.FAIL), ETopics.FINISH_FAIL),
            Map.entry( new SagaKey(EEventSource.PRODUCT_VALIDATION_SERVICE, ESagaStatus.ROLLBACK_PENDING), ETopics.PRODUCT_VALIDATION_FAIL),
            Map.entry( new SagaKey(EEventSource.PAYMENT_SERVICE, ESagaStatus.SUCCESS), ETopics.INVENTORY_SUCCESS),
            Map.entry( new SagaKey(EEventSource.PAYMENT_SERVICE, ESagaStatus.FAIL), ETopics.PRODUCT_VALIDATION_FAIL),
            Map.entry( new SagaKey(EEventSource.PAYMENT_SERVICE, ESagaStatus.ROLLBACK_PENDING), ETopics.PAYMENT_FAIL),
            Map.entry( new SagaKey(EEventSource.INVENTORY_SERVICE, ESagaStatus.SUCCESS), ETopics.FINISH_SUCCESS),
            Map.entry( new SagaKey(EEventSource.INVENTORY_SERVICE, ESagaStatus.FAIL), ETopics.PAYMENT_FAIL),
            Map.entry( new SagaKey(EEventSource.INVENTORY_SERVICE, ESagaStatus.ROLLBACK_PENDING), ETopics.INVENTORY_FAIL)
    );

    private static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID %s | EVENT ID %s";

    public ETopics getNextTopic(Event event){
        event.validate();
        SagaKey key = new SagaKey(event.getSource(),event.getStatus());
        ETopics nextTopic = SAGA_HANDLER.get(key);
        logCurrentSaga(event, nextTopic);
        return nextTopic;
    }

    private void logCurrentSaga(Event event, ETopics topic) {
        var sagaId = createSagaId(event);
        var source = event.getSource();
        switch (event.getStatus()) {
            case SUCCESS -> log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}",
                    source, topic, sagaId);
            case ROLLBACK_PENDING -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}",
                    source, topic, sagaId);
            case FAIL -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {}",
                    source, topic, sagaId);
        }
    }

    private String createSagaId(Event event) {
        return format(SAGA_LOG_ID,
                event.getPayload().getId(), event.getTransactionId(), event.getId());
    }
}
