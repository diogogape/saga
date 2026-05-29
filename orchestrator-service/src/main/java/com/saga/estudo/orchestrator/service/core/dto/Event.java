package com.saga.estudo.orchestrator.service.core.dto;


import com.saga.estudo.orchestrator.service.core.enums.EEventSource;
import com.saga.estudo.orchestrator.service.core.enums.ESagaStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private EEventSource source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;
}
