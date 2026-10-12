package com.eventbooking.venue;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private int capacity;

    protected Venue()
    {
        //for jpa
    }

    public Venue(String name, String address, String city, int capacity)
    {
        update(name, address, city, capacity);
    }

    public void update(String name, String address, String city, int capacity)
    {
        this.name = name;
        this.address = address;
        this.city = city;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public int getCapacity() { return capacity; }

}
