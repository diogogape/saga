package com.saga.estudo.orchestrator.service.core.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    private Product product;
    private Long quantity;
}
