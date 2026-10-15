package com.eventbooking.room;

public record RoomResponse(Long id, Long venueId, String name, int capacity) {

    public static RoomResponse from(Room r) {
        return new RoomResponse(r.getId(), r.getVenue().getId(), r.getName(), r.getCapacity());
    }
}
