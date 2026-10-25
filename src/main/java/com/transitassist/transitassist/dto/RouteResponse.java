package com.transitassist.transitassist.dto;

import com.transitassist.transitassist.entity.Route;

import java.util.List;

/** A route with its stops already in travel order. */
public record RouteResponse(Long id, String routeNumber, String name, String origin, String destination,
                            List<StopResponse> stops) {

    public static RouteResponse from(Route r, List<StopResponse> stops) {
        return new RouteResponse(r.getId(), r.getRouteNumber(), r.getName(), r.getOrigin(), r.getDestination(), stops);
    }
}
