package com.saga.estudo.order.service.dto;

import com.saga.estudo.order.service.document.Event;

public record EventNotification(String transactionId,
                                String orderId, String status) {

    public EventNotification() {
        this("","","");
    }

    public static EventNotification fromEvent(Event event){
        if (event == null){
            return new EventNotification();
        }else{
            return  new EventNotification(event.getTransactionId(),event.getOrderId(),event.getStatus());
        }
    }

}


