package com.saga.estudo.order.service.controller;

import com.saga.estudo.order.service.document.Event;
import com.saga.estudo.order.service.dto.EventFilters;
import com.saga.estudo.order.service.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public Event findByFilters(EventFilters eventFilters){
        return eventService.findByFilters(eventFilters);
    }

    @GetMapping("all")
    public List<Event> findAll(){
        return eventService.findAll();
    }
}
