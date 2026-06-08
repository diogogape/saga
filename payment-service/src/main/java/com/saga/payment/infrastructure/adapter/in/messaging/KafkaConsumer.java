package com.saga.payment.infrastructure.adapter.in.messaging;




import com.saga.payment.application.port.in.ProcessPaymentUseCase;
import com.saga.payment.application.port.in.RollbackPaymentUseCase;
import com.saga.payment.infrastructure.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {




    private final JsonUtil utils;


    private final ProcessPaymentUseCase processPayment;

    private final RollbackPaymentUseCase rollbackPayment;



    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.payment-success}"
    )
    public void paymentSuccessConsumer(String payload){
        log.info("receive event {} from payment-success topic", payload);
        var event = utils.toEvent(payload);
        processPayment.processPayment(event);

    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.payment-fail}"
    )
    public void paymentFailConsumer(String payload){
        log.info("receive event {} from payment-fail topic", payload);
        var event = utils.toEvent(payload);
        rollbackPayment.rollbackPayment(event);
    }



}
