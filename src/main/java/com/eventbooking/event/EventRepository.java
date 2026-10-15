package com.eventbooking.event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Published events in a room that overlap [from, to).
     * Used both to reject double bookings and for the "busy slots" endpoint.
     */
    @Query("""
            select e from Event e
            where e.room.id = :roomId
              and e.status = com.eventbooking.event.EventStatus.PUBLISHED
              and e.slot.startsAt < :to
              and e.slot.endsAt > :from
            order by e.slot.startsAt
            """)
    List<Event> findOverlapping(@Param("roomId") Long roomId,
                                @Param("from") Instant from,
                                @Param("to") Instant to);

    //loads room + venue in the same query
    @EntityGraph(attributePaths = {"room", "room.venue"})
    @Query(value = """
            select e from Event e
            where e.status = com.eventbooking.event.EventStatus.PUBLISHED
              and e.slot.startsAt >= :from
            order by e.slot.startsAt
            """,
           countQuery = """
            select count(e) from Event e
            where e.status = com.eventbooking.event.EventStatus.PUBLISHED
              and e.slot.startsAt >= :from
            """)
    Page<Event> findUpcoming(@Param("from") Instant from, Pageable pageable);

    @EntityGraph(attributePaths = {"room", "room.venue"})
    @Query(value = """
            select e from Event e
            where e.status = com.eventbooking.event.EventStatus.PUBLISHED
              and e.slot.startsAt >= :from
              and lower(e.room.venue.city) = lower(:city)
            order by e.slot.startsAt
            """,
           countQuery = """
            select count(e) from Event e
            where e.status = com.eventbooking.event.EventStatus.PUBLISHED
              and e.slot.startsAt >= :from
              and lower(e.room.venue.city) = lower(:city)
            """)
    Page<Event> findUpcomingInCity(@Param("city") String city, @Param("from") Instant from, Pageable pageable);

    @EntityGraph(attributePaths = {"room", "room.venue"})
    Optional<Event> findWithRoomById(Long id);

    boolean existsByRoomId(Long roomId);
}
