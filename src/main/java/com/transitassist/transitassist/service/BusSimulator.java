package com.transitassist.transitassist.service;

import com.transitassist.transitassist.dto.BusResponse;
import com.transitassist.transitassist.entity.Route;
import com.transitassist.transitassist.entity.RouteStop;
import com.transitassist.transitassist.repository.RouteStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.*;

/**
 * Builds the SIMULATED fleet: two buses (A and B) per route, derived from the routes/stops in the database.
 * Buses are not stored; each call recomputes positions from the current time (see RoutePath).
 */
@Service
@Transactional(readOnly = true)
public class BusSimulator {

    private static final double[] OFFSET_FRACTIONS = {0.0, 0.5};
    private static final String[] SUFFIXES = {"A", "B"};

    private final RouteStopRepository routeStopRepository;
    private final Clock clock;

    public BusSimulator(RouteStopRepository routeStopRepository, Clock clock) {
        this.routeStopRepository = routeStopRepository;
        this.clock = clock;
    }

    public List<BusResponse> allBuses() {
        long now = clock.millis();
        Map<Long, List<RouteStop>> byRoute = new LinkedHashMap<>();
        for (RouteStop rs : routeStopRepository.findAllOrdered()) {
            byRoute.computeIfAbsent(rs.getRoute().getId(), k -> new ArrayList<>()).add(rs);
        }

        List<BusResponse> buses = new ArrayList<>();
        for (List<RouteStop> routeStops : byRoute.values()) {
            if (routeStops.size() < 2) {
                continue; // a bus needs at least two stops to travel between
            }
            Route route = routeStops.get(0).getRoute();
            RoutePath path = new RoutePath(routeStops.stream()
                    .map(rs -> new RoutePath.PathStop(rs.getStop().getId(), rs.getStop().getName(),
                            rs.getStop().getLatitude(), rs.getStop().getLongitude()))
                    .toList());
            for (int i = 0; i < OFFSET_FRACTIONS.length; i++) {
                buses.add(buildBus(route, path, i, now));
            }
        }
        return buses;
    }

    public Optional<BusResponse> find(String code) {
        return allBuses().stream().filter(b -> b.code().equals(code)).findFirst();
    }

    private BusResponse buildBus(Route route, RoutePath path, int index, long now) {
        String code = route.getRouteNumber() + "-" + SUFFIXES[index];
        RoutePath.State state = path.stateAt(OFFSET_FRACTIONS[index], now);
        var upcoming = path.upcoming(state);
        String destination = state.forward() ? path.lastStopName() : path.firstStopName();
        return new BusResponse(code, route.getId(), route.getRouteNumber(), route.getName(), destination,
                state.latitude(), state.longitude(), occupancyFor(code, now),
                upcoming.isEmpty() ? null : upcoming.get(0).name(),
                upcoming.isEmpty() ? null : upcoming.get(0).etaSeconds(),
                upcoming, true);
    }

    /** Deterministic pseudo-random crowding that changes every minute. Not measured data. */
    private Occupancy occupancyFor(String code, long nowMillis) {
        int minuteBucket = (int) (nowMillis / 60_000);
        int h = Math.floorMod(Objects.hash(code, minuteBucket), Occupancy.values().length);
        return Occupancy.values()[h];
    }
}
