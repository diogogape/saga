package com.saga.estudo.inventory.service.service;

import com.saga.estudo.inventory.service.config.exception.ValidationException;
import com.saga.estudo.inventory.service.dto.Event;
import com.saga.estudo.inventory.service.dto.History;
import com.saga.estudo.inventory.service.dto.Order;
import com.saga.estudo.inventory.service.dto.OrderProduct;
import com.saga.estudo.inventory.service.enums.ESagaStatus;
import com.saga.estudo.inventory.service.model.Inventory;
import com.saga.estudo.inventory.service.model.OrderInventory;
import com.saga.estudo.inventory.service.producer.KafkaProducer;
import com.saga.estudo.inventory.service.repository.InventoryRepository;
import com.saga.estudo.inventory.service.repository.OrderInventoryRepository;
import com.saga.estudo.inventory.service.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class InventoryService {

    private static final String SOURCE ="INVENTORY_SERVICE";


    @Value("${kafka.topic.orchestrator}")
    private String orchestratorTopic;

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private OrderInventoryRepository orderInventoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;


    public void updateInventory(Event event){

        try {
            event.validate();
            validateExistOrderInventory(event);
            createOrderInventory(event);
            updateInventory(event.getPayload());
            handleSuccess(event);
        } catch (Exception e) {
            log.error("Error trying update inventory", e);
            handleFailCurrentUpdateInventory(event, e.getMessage());
        }
        kafkaProducer.sendEvent(orchestratorTopic, jsonUtil.toJson(event));

    }

    public void rollbackInvetory(Event event){
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(SOURCE);
        try {
            returnInventoryToPreviousValues(event);
            addHistory(event,"Rollback inventory ok ");
        }catch (Exception e) {
            log.error("Error trying rollback inventory", e);
            addHistory(event,"Fail on rollback inventory: ".concat(e.getMessage()));

        }
        kafkaProducer.sendEvent(orchestratorTopic, jsonUtil.toJson(event));

    }

    private void returnInventoryToPreviousValues(Event event) {
        orderInventoryRepository
                .findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                .forEach(orderInventory -> {
                    var inventory = orderInventory.getInventory();
                    inventory.setAvailable(orderInventory.getOldQuantity());
                    inventoryRepository.save(inventory);
                    log.info("Restored inventory for order {}: from {} to {}",
                            event.getPayload().getId(), orderInventory.getNewQuantity(), inventory.getAvailable());
                });
    }

    private void handleFailCurrentUpdateInventory(Event event, String message) {
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(SOURCE);
        addHistory(event,"Fail update inventory: ".concat(message));
    }

    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(SOURCE);
        addHistory(event, "Update Inventory successfully");
    }

    private void addHistory(Event event, String message) {
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void createOrderInventory(Event event) {
        event.getPayload().getProducts().forEach(orderProduct -> {
            createOrderInventory(event, orderProduct, findInventoryByProductCode(orderProduct.getProduct().getCode()));

        });
    }

    private void createOrderInventory(Event event, OrderProduct orderProduct, Inventory inventory) {
        OrderInventory orderInventory = OrderInventory.builder()
                .inventory(inventory)
                .oldQuantity(inventory.getAvailable())
                .orderQuantity(orderProduct.getQuantity())
                .newQuantity(inventory.getAvailable()-orderProduct.getQuantity())
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .build();
        orderInventoryRepository.save(orderInventory);

    }

    private void updateInventory(Order order) {
        order.getProducts().forEach(product -> {
            var inventory = findInventoryByProductCode(product.getProduct().getCode());
            checkInventory(inventory.getAvailable(), product.getQuantity());
            inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
            inventoryRepository.save(inventory);
        });
    }


    private void checkInventory(Long available, Long orderQuantity) {

        if (orderQuantity > available){
            throw new ValidationException("Items from inventory less than order quantity");
        }
    }

    private Inventory findInventoryByProductCode(String productCode){
        return inventoryRepository.findByProductCode(productCode)
                .orElseThrow(()-> new ValidationException("Inventory not found"));
    }

    private void validateExistOrderInventory(Event event) {
        if (orderInventoryRepository.existsByOrderIdAndTransactionId(event.getOrderId(),event.getTransactionId())){
            throw new ValidationException("There is another transactionId for this OrderInventory");
        }
    }

}
