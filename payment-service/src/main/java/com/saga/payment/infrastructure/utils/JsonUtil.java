package com.saga.payment.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import com.saga.payment.domain.model.Event;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            log.error("Error on covert object to json");
            throw ex;
        }
    }

    public Event toEvent(String json) {
        try {
            return objectMapper.readValue(json, Event.class);
        } catch (Exception ex) {
            log.error("Error on covert json to object");
            throw ex;
        }
    }
}
