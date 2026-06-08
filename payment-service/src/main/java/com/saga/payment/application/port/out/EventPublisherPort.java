package com.saga.payment.application.port.out;

import com.saga.payment.domain.model.Event;

public interface EventPublisherPort {
    void sendEvent(Event event);
}
