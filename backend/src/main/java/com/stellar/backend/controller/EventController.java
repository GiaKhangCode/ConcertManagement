package com.stellar.backend.controller;

import com.stellar.backend.dto.EventDetailDto;
import com.stellar.backend.dto.EventResponseDto;
import com.stellar.backend.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    // Constructor Injection
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponseDto> getEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventDetailDto getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }
}
