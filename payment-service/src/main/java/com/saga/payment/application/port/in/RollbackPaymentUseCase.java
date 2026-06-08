package com.saga.payment.application.port.in;

import com.saga.payment.domain.model.Event;

public interface RollbackPaymentUseCase {
    void rollbackPayment(Event event);
}
