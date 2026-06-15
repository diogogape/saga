package com.saga.payment.infrastructure.adapter.in.messaging;

import com.saga.payment.application.port.in.ProcessPaymentUseCase;
import com.saga.payment.application.port.in.RollbackPaymentUseCase;
import com.saga.payment.domain.model.Event;
import com.saga.payment.infrastructure.utils.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConsumer (adapter de entrada)")
class KafkaConsumerTest {

    @Mock
    private JsonUtil utils;
    @Mock
    private ProcessPaymentUseCase processPayment;
    @Mock
    private RollbackPaymentUseCase rollbackPayment;

    @InjectMocks
    private KafkaConsumer consumer;

    @Test
    @DisplayName("tópico payment-success desserializa e delega para processPayment")
    void paymentSuccessConsumer_shouldDelegateToProcess() {
        Event event = new Event();
        when(utils.toEvent("payload")).thenReturn(event);

        consumer.paymentSuccessConsumer("payload");

        verify(processPayment).processPayment(event);
        verifyNoInteractions(rollbackPayment);
    }

    @Test
    @DisplayName("tópico payment-fail desserializa e delega para rollbackPayment")
    void paymentFailConsumer_shouldDelegateToRollback() {
        Event event = new Event();
        when(utils.toEvent("payload")).thenReturn(event);

        consumer.paymentFailConsumer("payload");

        verify(rollbackPayment).rollbackPayment(event);
        verifyNoInteractions(processPayment);
    }
}
