package com.eventbooking.room;

import com.eventbooking.common.ConflictException;
import com.eventbooking.common.NotFoundException;
import com.eventbooking.event.EventRepository;
import com.eventbooking.venue.Venue;
import com.eventbooking.venue.VenueService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomService {

    private final RoomRepository rooms;
    private final VenueService venueService;
    private final EventRepository events;

    public RoomService(RoomRepository rooms, VenueService venueService, EventRepository events) {
        this.rooms = rooms;
        this.venueService = venueService;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> list(Long venueId) {
        venueService.find(venueId); // 404 for an unknown venue instead of an empty list
        return rooms.findByVenueIdOrderByNameAsc(venueId).stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long venueId, Long roomId) {
        return RoomResponse.from(find(venueId, roomId));
    }

    public RoomResponse create(Long venueId, RoomRequest req) {
        Venue venue = venueService.find(venueId);
        if (rooms.existsByVenueIdAndNameIgnoreCase(venueId, req.name())) {
            throw new ConflictException("Venue already has a room named '" + req.name() + "'");
        }
        Room room = rooms.save(new Room(venue, req.name(), req.capacity()));
        return RoomResponse.from(room);
    }

    public RoomResponse update(Long venueId, Long roomId, RoomRequest req) {
        Room room = find(venueId, roomId);
        if (rooms.existsByVenueIdAndNameIgnoreCaseAndIdNot(venueId, req.name(), roomId)) {
            throw new ConflictException("Venue already has a room named '" + req.name() + "'");
        }
        room.update(req.name(), req.capacity());
        return RoomResponse.from(room);
    }

    public void delete(Long venueId, Long roomId) {
        Room room = find(venueId, roomId);
        if (events.existsByRoomId(roomId)) {
            throw new ConflictException("Room has events and cannot be deleted");
        }
        rooms.delete(room);
    }

    public Room find(Long venueId, Long roomId) {
        return rooms.findByIdAndVenueId(roomId, venueId)
                .orElseThrow(() -> new NotFoundException("Room", roomId));
    }

    public Room findById(Long roomId) {
        return rooms.findById(roomId).orElseThrow(() -> new NotFoundException("Room", roomId));
    }
}
