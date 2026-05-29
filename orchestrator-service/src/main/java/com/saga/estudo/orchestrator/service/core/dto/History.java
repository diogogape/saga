package com.saga.estudo.orchestrator.service.core.dto;

import com.saga.estudo.orchestrator.service.core.enums.EEventSource;
import com.saga.estudo.orchestrator.service.core.enums.ESagaStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {

    private EEventSource source;
    private ESagaStatus status;
    private String message;
    private LocalDateTime createdAt;
}
