package com.eventbooking.room;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByVenueIdOrderByNameAsc(Long venueId);

    Optional<Room> findByIdAndVenueId(Long id, Long venueId);

    boolean existsByVenueIdAndNameIgnoreCase(Long venueId, String name);

    boolean existsByVenueIdAndNameIgnoreCaseAndIdNot(Long venueId, String name, Long id);

    boolean existsByVenueId(Long venueId);

    @Query("select coalesce(max(r.capacity), 0) from Room r where r.venue.id = :venueId")
    int maxCapacityForVenue(@Param("venueId") Long venueId);
}
