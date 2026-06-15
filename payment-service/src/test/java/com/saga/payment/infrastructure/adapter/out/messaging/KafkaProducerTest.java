package com.saga.payment.infrastructure.adapter.out.messaging;

import com.saga.payment.domain.model.Event;
import com.saga.payment.infrastructure.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducer (adapter de saída)")
class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private JsonUtil jsonUtil;

    @InjectMocks
    private KafkaProducer producer;

    private static final String TOPIC = "orchestrator";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "orchestratorTopic", TOPIC);
    }

    @Test
    @DisplayName("serializa o evento e publica no tópico do orquestrador")
    void sendEvent_shouldPublishSerializedEvent() {
        Event event = new Event();
        when(jsonUtil.toJson(event)).thenReturn("{json}");

        producer.sendEvent(event);

        verify(kafkaTemplate).send(eq(TOPIC), eq("{json}"));
    }

    @Test
    @DisplayName("falha no envio não propaga exceção (comportamento atual)")
    void sendEvent_shouldNotPropagateWhenSendFails() {
        Event event = new Event();
        when(jsonUtil.toJson(event)).thenReturn("{json}");
        when(kafkaTemplate.send(eq(TOPIC), eq("{json}")))
                .thenThrow(new RuntimeException("broker indisponível"));

        assertThatCode(() -> producer.sendEvent(event)).doesNotThrowAnyException();
    }
}
