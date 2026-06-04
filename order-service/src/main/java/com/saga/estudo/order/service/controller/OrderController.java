package com.saga.estudo.order.service.controller;

import com.saga.estudo.order.service.document.Order;
import com.saga.estudo.order.service.dto.OrderRequest;
import com.saga.estudo.order.service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest orderRequest){
        return  orderService.createorder(orderRequest);
    }
}
