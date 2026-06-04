package com.saga.estudo.order.service.service;

import com.saga.estudo.order.service.document.Event;
import com.saga.estudo.order.service.document.Order;
import com.saga.estudo.order.service.dto.OrderRequest;
import com.saga.estudo.order.service.producer.KafkaProducer;
import com.saga.estudo.order.service.repository.OrderRepository;
import com.saga.estudo.order.service.utils.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private static final String TRANSATION_IS_FORMAT = "%s_%s";
    private final KafkaProducer kafkaProducer;
    private final OrderRepository orderRepository;
    private final EventService eventService;
    private final JsonUtil jsonUtil;

    public Order createorder(OrderRequest orderRequest){
        Order order = Order.builder()
                .products(orderRequest.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(
                        String.format(TRANSATION_IS_FORMAT, Instant.now(), UUID.randomUUID())
                )
                .build();
        orderRepository.save(order);
        kafkaProducer.sendEvent(jsonUtil.toJson(createPayLoad(order)));
        return order;
    }

    private Event createPayLoad(Order order){
        Event event = Event.builder()
                .createdAt(LocalDateTime.now())
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .build();
        eventService.save(event);
        return event;
    }
}
