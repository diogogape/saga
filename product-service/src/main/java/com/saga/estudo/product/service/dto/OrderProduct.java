package com.saga.estudo.product.service.dto;


import com.saga.estudo.product.service.config.exception.ValidationException;
import lombok.*;
import org.springframework.util.ObjectUtils;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    private Product product;
    private Long quantity;

    public void validate(){
        if(ObjectUtils.isEmpty(product)){
            throw new ValidationException("Product not informed");
        }
        product.validate();
    }
}
