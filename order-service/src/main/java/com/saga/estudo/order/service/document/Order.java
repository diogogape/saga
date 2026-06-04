package com.saga.estudo.order.service.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection  = "order")
public class Order {
    @Id
    private String id;
    private List<OrderProduct> products;
    private Double totalAmount;
    private Long totalItems;
    private LocalDateTime createdAt;
    private String transactionId;

}
