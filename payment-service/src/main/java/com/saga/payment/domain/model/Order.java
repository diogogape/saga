package com.saga.payment.domain.model;


import com.saga.payment.domain.exception.ValidationException;
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

    private final static Double DOUBLE_ZERO = 0.0;


    public void validate (){
        if (ObjectUtils.isEmpty(products)||products.isEmpty()){
            throw new ValidationException("Products not informed.");
        }
        products.forEach(OrderProduct::validate);
    }

    public Double calculateAmount() {
        totalAmount = products.stream().map(product->
            product.getQuantity() * product.getProduct().getUnitValue()
        ).reduce(DOUBLE_ZERO, Double::sum);
        return totalAmount;
    }

    public Long calculateTotalItems() {
        totalItems = products.stream().map(OrderProduct::getQuantity
        ).reduce(DOUBLE_ZERO.longValue(), Long::sum);
        return totalItems;
    }
}
