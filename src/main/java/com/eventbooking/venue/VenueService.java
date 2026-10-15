package com.eventbooking.venue;

import com.eventbooking.common.BusinessRuleException;
import com.eventbooking.common.ConflictException;
import com.eventbooking.common.NotFoundException;
import com.eventbooking.common.PageResponse;
import com.eventbooking.room.RoomRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VenueService {

    private final VenueRepository venues;
    private final RoomRepository rooms;

    public VenueService(VenueRepository venues, RoomRepository rooms) {
        this.venues = venues;
        this.rooms = rooms;
    }

    @Transactional(readOnly = true)
    public PageResponse<VenueResponse> list(Pageable pageable) {
        return PageResponse.of(venues.findAll(pageable), VenueResponse::from);
    }

    @Transactional(readOnly = true)
    public VenueResponse get(Long id) {
        return VenueResponse.from(find(id));
    }

    public VenueResponse create(VenueRequest req) {
        Venue venue = new Venue(req.name(), req.address(), req.city(), req.capacity());
        return VenueResponse.from(venues.save(venue));
    }

    public VenueResponse update(Long id, VenueRequest req) {
        Venue venue = find(id);
        int largestRoom = rooms.maxCapacityForVenue(id);
        if (req.capacity() < largestRoom) {
            throw new BusinessRuleException(
                    "Venue capacity cannot be lower than its largest room (" + largestRoom + ")");
        }
        venue.update(req.name(), req.address(), req.city(), req.capacity());
        return VenueResponse.from(venue);
    }

    public void delete(Long id) {
        Venue venue = find(id);
        if (rooms.existsByVenueId(id)) {
            throw new ConflictException("Venue has rooms and cannot be deleted");
        }
        venues.delete(venue);
    }

    public Venue find(Long id) {
        return venues.findById(id).orElseThrow(() -> new NotFoundException("Venue", id));
    }
}
