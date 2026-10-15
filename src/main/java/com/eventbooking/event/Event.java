package com.eventbooking.event;

import com.eventbooking.common.BusinessRuleException;
import com.eventbooking.common.ConflictException;
import com.eventbooking.room.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Something happening in a room during a time slot. Many events -> one room. */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Embedded
    private TimeSlot slot;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    protected Event() {
        // for JPA
    }

    public Event(Room room, String title, String description, TimeSlot slot, int capacity, BigDecimal price) {
        this.status = EventStatus.PUBLISHED;
        update(room, title, description, slot, capacity, price);
    }

    public void update(Room room, String title, String description, TimeSlot slot, int capacity, BigDecimal price) {
        if (status == EventStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled events cannot be changed");
        }
        if (capacity > room.getCapacity()) {
            throw new BusinessRuleException(
                    "Event capacity (" + capacity + ") exceeds room capacity (" + room.getCapacity() + ")");
        }
        this.room = room;
        this.title = title;
        this.description = description;
        this.slot = slot;
        this.capacity = capacity;
        this.price = price;
    }

    public void cancel() {
        if (status == EventStatus.CANCELLED) {
            throw new ConflictException("Event is already cancelled");
        }
        status = EventStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public Room getRoom() { return room; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TimeSlot getSlot() { return slot; }
    public int getCapacity() { return capacity; }
    public BigDecimal getPrice() { return price; }
    public EventStatus getStatus() { return status; }
}
