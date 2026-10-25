package com.transitassist.transitassist.dto;

import com.transitassist.transitassist.entity.StopRequest;
import com.transitassist.transitassist.entity.StopRequestStatus;

import java.time.Instant;

public record StopRequestResponse(Long id, String busCode, String routeNumber, Long stopId, String stopName,
                                  StopRequestStatus status, Instant createdAt, String notice) {

    private static final String NOTICE =
            "Recorded in the database only. No driver has been notified; this prototype has no driver app.";

    public static StopRequestResponse from(StopRequest r) {
        return new StopRequestResponse(r.getId(), r.getBusCode(), r.getRoute().getRouteNumber(),
                r.getStop().getId(), r.getStop().getName(), r.getStatus(), r.getCreatedAt(), NOTICE);
    }
}
