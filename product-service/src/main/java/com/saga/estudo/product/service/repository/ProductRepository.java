package com.saga.estudo.product.service.repository;

import com.saga.estudo.product.service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Boolean existsByCode(String code);
}
