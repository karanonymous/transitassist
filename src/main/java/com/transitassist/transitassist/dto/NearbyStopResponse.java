package com.transitassist.transitassist.dto;

public record NearbyStopResponse(Long id, String name, String locality, double latitude, double longitude,
                                 int distanceMeters) {
}
