package com.saga.estudo.product.service.dto;


import com.saga.estudo.product.service.config.exception.ValidationException;
import lombok.*;
import org.springframework.util.ObjectUtils;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String code;
    private Double unitValue;

    public void validate() {
        if (ObjectUtils.isEmpty(code)){
            throw new ValidationException("Product code not informed");
        }
    }
}
