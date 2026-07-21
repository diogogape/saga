package com.saga.estudo.order.service.service;

import com.saga.estudo.order.service.config.exception.ValidationException;
import com.saga.estudo.order.service.document.Event;
import com.saga.estudo.order.service.dto.EventNotification;
import com.saga.estudo.order.service.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotificationService {

    // O mapa de conexões agora vive isolado na camada de serviço
    private final Map<String, SseEmitter> localEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final EventService eventService;

    public NotificationService(EventService eventService) {
        this.eventService = eventService;
        // Heartbeat para manter as conexões HTTP vivas a cada 30 segundos
        this.heartbeatExecutor.scheduleAtFixedRate(this::enviarHeartbeat, 30, 30, TimeUnit.SECONDS);
    }

    public SseEmitter criarConexao(String orderId) {

        SseEmitter emitter = new SseEmitter(300000L); // 5 minutos de timeout

        localEmitters.put(orderId, emitter);
        log.info("New connection SSE for orderId: {}", orderId);

        // Callbacks de limpeza automática da memória
        emitter.onCompletion(() -> localEmitters.remove(orderId));
        emitter.onTimeout(() -> localEmitters.remove(orderId));
        emitter.onError((e) -> localEmitters.remove(orderId));

        try {
            emitter.send(SseEmitter.event());
        } catch (IOException e) {
            localEmitters.remove(orderId);
        }

        try {
            Event event = eventService.findByOrderIdAndSource(orderId,"ORCHESTRATOR");
            log.info("Event processed orderId: {}", orderId);
            despacharEEncerrar(orderId,event);

        } catch (ValidationException validationException){
          log.info("Event not processed");
        }

        return emitter;
    }

    public void despacharEEncerrar(String orderId, Event event)
    {
        SseEmitter emitter = localEmitters.get(orderId);
        if (emitter != null) {
            try {
                EventNotification payload =  EventNotification.fromEvent(event);
                emitter.send(SseEmitter.event().data(payload));
                emitter.complete(); // Fecha a conexão HTTP de forma limpa
                log.info("SSE send and finish for orderId: {}", orderId);
                log.info("SSE send and finish for event: {}", payload);
            } catch (IOException e) {
                log.error("Error on SSE", e);
            } finally {
                localEmitters.remove(orderId);
            }
        }
    }

    private void enviarHeartbeat() {
        localEmitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("PING").data("keep-alive"));
            } catch (IOException e) {
                log.debug("Delete connection from  Heartbeat: {}", id);
                localEmitters.remove(id);
            }
        });
    }
}
