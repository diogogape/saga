package com.saga.estudo.order.service.repository;

import com.saga.estudo.order.service.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
