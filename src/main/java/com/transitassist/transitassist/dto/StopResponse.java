package com.transitassist.transitassist.dto;

import com.transitassist.transitassist.entity.Stop;

public record StopResponse(Long id, String name, String locality, double latitude, double longitude) {

    public static StopResponse from(Stop s) {
        return new StopResponse(s.getId(), s.getName(), s.getLocality(), s.getLatitude(), s.getLongitude());
    }
}
