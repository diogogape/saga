package com.saga.estudo.order.service.repository;

import com.saga.estudo.order.service.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;


import java.util.List;
import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findAllByOrderByCreatedAtDesc();

    Optional<Event> findTop1ByTransactionIdOrderByCreatedAtDesc(String transactionId);

    Optional<Event> findTop1ByOrderIdOrderByCreatedAtDesc(String orderId);

    Optional<Event> findTop1ByOrderIdAndSourceOrderByCreatedAtDesc(String orderId,String source);

    Optional<Event> findTop1ByOrderIdAndTransactionIdOrderByCreatedAtDesc(String orderId, String transactionId);
}
