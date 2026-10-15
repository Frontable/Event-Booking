package com.eventbooking.event;

import com.eventbooking.common.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping("/api/events")
    public PageResponse<EventResponse> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.listUpcoming(city, from, pageable);
    }

    @GetMapping("/api/events/{id}")
    public EventResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/api/events")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest req) {
        EventResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/events/" + created.id())).body(created);
    }

    @PutMapping("/api/events/{id}")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/api/events/{id}/cancel")
    public EventResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/api/rooms/{roomId}/busy")
    public List<BusySlotResponse> busy(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.busySlots(roomId, from, to);
    }
}
