package com.eventbooking.event;

import com.eventbooking.room.Room;
import com.eventbooking.venue.Venue;
import java.math.BigDecimal;
import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        Long roomId,
        String roomName,
        Long venueId,
        String venueName,
        String city,
        Instant startsAt,
        Instant endsAt,
        int capacity,
        BigDecimal price,
        EventStatus status) {

    public static EventResponse from(Event e) {
        Room room = e.getRoom();
        Venue venue = room.getVenue();
        return new EventResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                room.getId(),
                room.getName(),
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                e.getSlot().startsAt(),
                e.getSlot().endsAt(),
                e.getCapacity(),
                e.getPrice(),
                e.getStatus());
    }
}
