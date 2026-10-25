package com.transitassist.transitassist.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_number", nullable = false, unique = true, length = 20)
    private String routeNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    protected Route() {
    }

    public Route(String routeNumber, String name, String origin, String destination) {
        this.routeNumber = routeNumber;
        this.name = name;
        this.origin = origin;
        this.destination = destination;
    }

    public Long getId() { return id; }
    public String getRouteNumber() { return routeNumber; }
    public String getName() { return name; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
}
