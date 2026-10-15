package com.eventbooking.event;

import java.time.Instant;

public record BusySlotResponse(Long eventId, String title, Instant startsAt, Instant endsAt) {

    public static BusySlotResponse from(Event e) {
        return new BusySlotResponse(e.getId(), e.getTitle(), e.getSlot().startsAt(), e.getSlot().endsAt());
    }
}
