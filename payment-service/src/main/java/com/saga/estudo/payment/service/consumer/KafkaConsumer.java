package com.saga.estudo.payment.service.consumer;




import com.saga.estudo.payment.service.service.PaymentService;
import com.saga.estudo.payment.service.utils.JsonUtil;
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
    private PaymentService paymentService;



    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.payment-success}"
    )
    public void finishSuccessConsumer(String payload){
        log.info("receive event {} from payment-success topic", payload);
        var event = utils.toEvent(payload);
        paymentService.realizePayment(event);

    }

    @KafkaListener(
            groupId ="${spring.kafka.consumer.group-id}",
            topics ="${kafka.topic.payment-fail}"
    )
    public void finishFailConsumer(String payload){
        log.info("receive event {} from payment-fail topic", payload);
        var event = utils.toEvent(payload);
        paymentService.rollbackPayment(event);
    }



}
