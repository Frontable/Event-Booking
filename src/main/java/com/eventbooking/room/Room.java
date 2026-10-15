package com.eventbooking.room;

import com.eventbooking.common.BusinessRuleException;
import com.eventbooking.venue.Venue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int capacity;

    protected Room() {
        // for jpa
    }

    public Room(Venue venue, String name, int capacity) {
        this.venue = venue;
        update(name, capacity);
    }

    public void update(String name, int capacity) {
        if (capacity > venue.getCapacity()) {
            throw new BusinessRuleException(
                    "Room capacity (" + capacity + ") exceeds venue capacity (" + venue.getCapacity() + ")");
        }
        this.name = name;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public Venue getVenue() { return venue; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
}
