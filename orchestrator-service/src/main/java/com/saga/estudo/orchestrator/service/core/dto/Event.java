package com.saga.estudo.orchestrator.service.core.dto;


import com.saga.estudo.orchestrator.service.config.exception.ValidationException;
import com.saga.estudo.orchestrator.service.core.enums.EEventSource;
import com.saga.estudo.orchestrator.service.core.enums.ESagaStatus;
import lombok.*;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {
    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private EEventSource source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void validate(){
        if (ObjectUtils.isEmpty(source)
                || ObjectUtils.isEmpty(orderId)){
            throw new ValidationException("source and status must be informed.");
        }

    }

    public void addToHistory(History history) {
        if (ObjectUtils.isEmpty(eventHistory)){
            eventHistory = new ArrayList<History>();
        }
        eventHistory.add(history);
    }
}
