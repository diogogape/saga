package com.saga.payment.application.port.in;

import com.saga.payment.domain.model.Event;

public interface ProcessPaymentUseCase {

    void processPayment(Event event);
}
