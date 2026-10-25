package com.transitassist.transitassist.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stops")
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String locality;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    protected Stop() {
    }

    public Stop(String name, String locality, double latitude, double longitude) {
        this.name = name;
        this.locality = locality;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocality() { return locality; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
