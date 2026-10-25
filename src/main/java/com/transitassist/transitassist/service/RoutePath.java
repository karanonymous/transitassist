package com.transitassist.transitassist.service;

import com.transitassist.transitassist.dto.UpcomingStop;
import com.transitassist.transitassist.util.GeoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure maths for one route: a polyline through its stops in order. A simulated bus shuttles back and forth
 * along it at a constant speed ("ping-pong"). Position is computed from the clock, so no state is stored and
 * no scheduler is needed: same time in = same position out.
 */
public final class RoutePath {

    /** About 21.6 km/h. Illustrative only. */
    public static final double SPEED_METERS_PER_SECOND = 6.0;

    public record PathStop(long stopId, String name, double latitude, double longitude) {
    }

    /** forward = heading toward the LAST stop; distanceMeters = distance from the FIRST stop along the path. */
    public record State(double latitude, double longitude, boolean forward, double distanceMeters) {
    }

    private final List<PathStop> stops;
    private final double[] cumulative; // distance from first stop to stop i

    public RoutePath(List<PathStop> stops) {
        if (stops.size() < 2) {
            throw new IllegalArgumentException("A route path needs at least 2 stops");
        }
        this.stops = List.copyOf(stops);
        this.cumulative = new double[stops.size()];
        for (int i = 1; i < stops.size(); i++) {
            PathStop a = stops.get(i - 1);
            PathStop b = stops.get(i);
            cumulative[i] = cumulative[i - 1] + GeoUtils.haversineMeters(a.latitude(), a.longitude(), b.latitude(), b.longitude());
        }
    }

    public double totalMeters() {
        return cumulative[cumulative.length - 1];
    }

    public String firstStopName() {
        return stops.get(0).name();
    }

    public String lastStopName() {
        return stops.get(stops.size() - 1).name();
    }

    /**
     * @param offsetFraction 0..1, where in the out-and-back cycle this bus starts (so buses are spread out)
     * @param epochMillis    current time
     */
    public State stateAt(double offsetFraction, long epochMillis) {
        double total = totalMeters();
        double cycle = 2 * total;
        double travelled = (epochMillis / 1000.0) * SPEED_METERS_PER_SECOND + offsetFraction * cycle;
        double d = travelled % cycle;                 // position within one out-and-back trip
        boolean forward = d < total;
        double dist = forward ? d : cycle - d;        // distance from first stop

        int i = 1;
        while (i < cumulative.length - 1 && cumulative[i] < dist) {
            i++;
        }
        PathStop a = stops.get(i - 1);
        PathStop b = stops.get(i);
        double segment = cumulative[i] - cumulative[i - 1];
        double f = segment == 0 ? 0 : Math.min(1, Math.max(0, (dist - cumulative[i - 1]) / segment));
        double lat = a.latitude() + (b.latitude() - a.latitude()) * f;
        double lng = a.longitude() + (b.longitude() - a.longitude()) * f;
        return new State(lat, lng, forward, dist);
    }

    /** Stops still ahead of the bus in its current direction, nearest first, with simulated ETAs. */
    public List<UpcomingStop> upcoming(State s) {
        List<UpcomingStop> result = new ArrayList<>();
        if (s.forward()) {
            for (int i = 0; i < stops.size(); i++) {
                if (cumulative[i] > s.distanceMeters() + 1e-6) {
                    result.add(toUpcoming(i, cumulative[i] - s.distanceMeters()));
                }
            }
        } else {
            for (int i = stops.size() - 1; i >= 0; i--) {
                if (cumulative[i] < s.distanceMeters() - 1e-6) {
                    result.add(toUpcoming(i, s.distanceMeters() - cumulative[i]));
                }
            }
        }
        return result;
    }

    private UpcomingStop toUpcoming(int index, double meters) {
        PathStop stop = stops.get(index);
        return new UpcomingStop(stop.stopId(), stop.name(), (int) Math.round(meters / SPEED_METERS_PER_SECOND));
    }
}
