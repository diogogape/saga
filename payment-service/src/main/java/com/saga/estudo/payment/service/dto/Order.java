package com.saga.estudo.payment.service.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private List<OrderProduct> products;
    private Double totalAmount;
    private Long totalItems;
    private LocalDateTime createdAt;
    private String transactionId;

}
