package com.eventbooking.event;

import com.eventbooking.common.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;


//A half-open time range [startsAt, endsAt). Embedded into the owning table as two columns.
@Embeddable
public record TimeSlot(
        @Column(name = "starts_at", nullable = false, columnDefinition = "timestamptz") Instant startsAt,
        @Column(name = "ends_at", nullable = false, columnDefinition = "timestamptz") Instant endsAt) {

    public TimeSlot {
        if (startsAt == null || endsAt == null) {
            throw new BusinessRuleException("Start and end are required");
        }
        if (!endsAt.isAfter(startsAt)) {
            throw new BusinessRuleException("Event must end after it starts");
        }
    }

    /** 10:00-12:00 and 12:00-14:00 do NOT overlap. */
    public boolean overlaps(TimeSlot other) {
        return startsAt.isBefore(other.endsAt) && other.startsAt.isBefore(endsAt);
    }

    public boolean hasStartedAt(Instant now) {
        return !now.isBefore(startsAt);
    }

    public Duration duration() {
        return Duration.between(startsAt, endsAt);
    }
}
