package com.saga.estudo.product.service.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String code;
    private Double unitValue;
}
