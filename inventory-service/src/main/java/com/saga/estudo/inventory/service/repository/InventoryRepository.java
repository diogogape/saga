package com.saga.estudo.inventory.service.repository;

import com.saga.estudo.inventory.service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductCode(String productCode);
}
