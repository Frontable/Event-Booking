package com.eventbooking.room;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues/{venueId}/rooms")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomResponse> list(@PathVariable Long venueId) {
        return service.list(venueId);
    }

    @GetMapping("/{roomId}")
    public RoomResponse get(@PathVariable Long venueId, @PathVariable Long roomId) {
        return service.get(venueId, roomId);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@PathVariable Long venueId, @Valid @RequestBody RoomRequest req) {
        RoomResponse created = service.create(venueId, req);
        URI location = URI.create("/api/venues/" + venueId + "/rooms/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{roomId}")
    public RoomResponse update(@PathVariable Long venueId, @PathVariable Long roomId,
                               @Valid @RequestBody RoomRequest req) {
        return service.update(venueId, roomId, req);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable Long venueId, @PathVariable Long roomId) {
        service.delete(venueId, roomId);
        return ResponseEntity.noContent().build();
    }
}
