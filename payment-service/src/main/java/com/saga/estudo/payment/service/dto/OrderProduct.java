package com.saga.estudo.payment.service.dto;


import com.saga.estudo.payment.service.config.exception.ValidationException;
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
        if(ObjectUtils.isEmpty(product) || ObjectUtils.isEmpty(quantity)){
            throw new ValidationException("Product or quantity not informed");
        }
        product.validate();
    }
}
