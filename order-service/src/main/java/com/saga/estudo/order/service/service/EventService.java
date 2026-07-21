package com.saga.estudo.order.service.service;

import com.saga.estudo.order.service.config.exception.ValidationException;
import com.saga.estudo.order.service.document.Event;
import com.saga.estudo.order.service.dto.EventFilters;
import com.saga.estudo.order.service.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnd(Event event){
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} notify end TransactionId:{}", event.getOrderId(), event.getTransactionId());
    }

    public Event save(Event event){
        return eventRepository.save(event);
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters eventFilters) {
        eventFilters.validateFilters();
        if(ObjectUtils.isEmpty(eventFilters.getOrderId())){
            return findByTransactionId(eventFilters.getTransactionId());
        }else if (ObjectUtils.isEmpty(eventFilters.getTransactionId())){
            return findByOrderId(eventFilters.getOrderId());
        }else{
            return findByOrderIdAnsTransactionId(eventFilters.getOrderId(), eventFilters.getTransactionId());
        }
    }

    private Event findByOrderIdAnsTransactionId(String orderId, String transactionId) {
        return eventRepository.findTop1ByOrderIdAndTransactionIdOrderByCreatedAtDesc(orderId, transactionId)
                .orElseThrow(()-> new ValidationException("Event not found by OrderId and TransactionId"));
    }

    public Event findByOrderId(String orderId) {

        return eventRepository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(()-> new ValidationException("Event not found by OrderId"));

    }

    public Event findByOrderIdAndSource(String orderId, String source){
        return eventRepository.findTop1ByOrderIdAndSourceOrderByCreatedAtDesc(orderId,source)
                .orElseThrow(()-> new ValidationException("Event not found by OrderId"));
    }

    private Event findByTransactionId(String transactionId) {
        return eventRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(()-> new ValidationException("Event not found by TransactionId"));
    }


}
