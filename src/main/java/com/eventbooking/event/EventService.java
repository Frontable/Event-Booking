package com.eventbooking.event;

import com.eventbooking.common.BusinessRuleException;
import com.eventbooking.common.ConflictException;
import com.eventbooking.common.NotFoundException;
import com.eventbooking.common.PageResponse;
import com.eventbooking.room.Room;
import com.eventbooking.room.RoomService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventService {

    private static final Duration MAX_BUSY_WINDOW = Duration.ofDays(31);

    private final EventRepository events;
    private final RoomService roomService;
    private final Clock clock;

    public EventService(EventRepository events, RoomService roomService, Clock clock) {
        this.events = events;
        this.roomService = roomService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> listUpcoming(String city, Instant from, Pageable pageable) {
        Instant start = from != null ? from : clock.instant();
        Page<Event> page = (city == null || city.isBlank())
                ? events.findUpcoming(start, pageable)
                : events.findUpcomingInCity(city.trim(), start, pageable);
        return PageResponse.of(page, EventResponse::from);
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) {
        return EventResponse.from(find(id));
    }

    public EventResponse create(EventRequest req) {
        Room room = roomService.findById(req.roomId());
        TimeSlot slot = new TimeSlot(req.startsAt(), req.endsAt());
        requireInFuture(slot);
        requireRoomFree(room.getId(), slot, null);

        Event event = new Event(room, req.title(), req.description(), slot, req.capacity(), req.price());
        return EventResponse.from(events.save(event));
    }

    public EventResponse update(Long id, EventRequest req) {
        Event event = find(id);
        Room room = roomService.findById(req.roomId());
        TimeSlot slot = new TimeSlot(req.startsAt(), req.endsAt());
        requireInFuture(slot);
        requireRoomFree(room.getId(), slot, id);

        event.update(room, req.title(), req.description(), slot, req.capacity(), req.price());
        return EventResponse.from(event);
    }

    public EventResponse cancel(Long id) {
        Event event = find(id);
        event.cancel();
        return EventResponse.from(event);
    }

    //Published events in the room during [from, to)
    @Transactional(readOnly = true)
    public List<BusySlotResponse> busySlots(Long roomId, Instant from, Instant to) {
        roomService.findById(roomId);
        if (!to.isAfter(from)) {
            throw new BusinessRuleException("'to' must be after 'from'");
        }
        if (Duration.between(from, to).compareTo(MAX_BUSY_WINDOW) > 0) {
            throw new BusinessRuleException("The window can be at most 31 days");
        }
        return events.findOverlapping(roomId, from, to).stream()
                .map(BusySlotResponse::from)
                .toList();
    }

    private Event find(Long id) {
        return events.findWithRoomById(id).orElseThrow(() -> new NotFoundException("Event", id));
    }

    private void requireInFuture(TimeSlot slot) {
        if (slot.hasStartedAt(clock.instant())) {
            throw new BusinessRuleException("Event must start in the future");
        }
    }

    //the event being updated, so it doesn't clash with itself (null when creating).
    private void requireRoomFree(Long roomId, TimeSlot slot, Long selfId) {
        events.findOverlapping(roomId, slot.startsAt(), slot.endsAt()).stream()
                .filter(other -> !other.getId().equals(selfId))
                .findFirst()
                .ifPresent(other -> {
                    throw new ConflictException("Room is already booked by '" + other.getTitle()
                            + "' from " + other.getSlot().startsAt() + " to " + other.getSlot().endsAt());
                });
    }
}
