package com.saga.estudo.orchestrator.service.core.enums;

import lombok.Getter;


public enum ESagaStatus {
    SUCCESS,
    ROLLBACK_PENDING,
    FAIL
}
