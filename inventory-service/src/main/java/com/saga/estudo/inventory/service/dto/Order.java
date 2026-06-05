package com.saga.estudo.inventory.service.dto;

import com.saga.estudo.inventory.service.config.exception.ValidationException;
import lombok.*;
import org.springframework.util.ObjectUtils;

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

    public void validate (){
        if (ObjectUtils.isEmpty(products)||products.isEmpty()){
            throw new ValidationException("Products not informed.");
        }
        products.forEach(OrderProduct::validate);
    }
}
