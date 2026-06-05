package com.saga.estudo.inventory.service.dto;


import com.saga.estudo.inventory.service.config.exception.ValidationException;
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
        if (ObjectUtils.isEmpty(code) || ObjectUtils.isEmpty(unitValue)){
            throw new ValidationException("Product code or unitValue not informed");
        }
    }
}
