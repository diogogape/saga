package com.saga.estudo.order.service.utils;

import tools.jackson.databind.ObjectMapper;
import com.saga.estudo.order.service.document.Event;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {


    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            log.error("Erro: {}", ex.getMessage(), ex);
            return "";
        }
    }

    public Event toEvent(String json) {
        try {
            return objectMapper.readValue(json, Event.class);
        } catch (Exception ex) {
            log.error("Erro: {}", ex.getMessage(), ex);
            return null;
        }
    }
}
