package com.transitassist.transitassist.entity;

import jakarta.persistence.*;

import java.time.Instant;

/** A commuter's "please stop for me" request. Persisted only; no driver is notified. */
@Entity
@Table(name = "stop_requests")
public class StopRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Simulated buses are not stored in the DB, so the bus is identified by its code, e.g. "101-A". */
    @Column(name = "bus_code", nullable = false, length = 40)
    private String busCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StopRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StopRequest() {
    }

    public StopRequest(String busCode, Route route, Stop stop, StopRequestStatus status, Instant createdAt) {
        this.busCode = busCode;
        this.route = route;
        this.stop = stop;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getBusCode() { return busCode; }
    public Route getRoute() { return route; }
    public Stop getStop() { return stop; }
    public StopRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
