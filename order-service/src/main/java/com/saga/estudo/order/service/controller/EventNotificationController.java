package com.saga.estudo.order.service.controller;


import com.saga.estudo.order.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;




@RestController
@RequestMapping("/api/event-notification")
@Slf4j
public class EventNotificationController {

    private final NotificationService notificationService;


    // Injeção de dependência padrão do Spring
    public EventNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter inscrever(@PathVariable String orderId) {
        // Apenas repassa a responsabilidade para o Service
        return notificationService.criarConexao(orderId);
    }

}
