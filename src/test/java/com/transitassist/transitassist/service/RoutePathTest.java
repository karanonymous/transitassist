package com.transitassist.transitassist.service;

import com.transitassist.transitassist.dto.UpcomingStop;
import com.transitassist.transitassist.util.GeoUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pure unit tests: no Spring, no database. */
class RoutePathTest {

    private final RoutePath path = new RoutePath(List.of(
            new RoutePath.PathStop(1, "A", 23.2000, 77.4000),
            new RoutePath.PathStop(2, "B", 23.2100, 77.4000),
            new RoutePath.PathStop(3, "C", 23.2200, 77.4000)));

    @Test
    void busStartsAtFirstStopHeadingForwardWithAllOtherStopsUpcoming() {
        RoutePath.State s = path.stateAt(0.0, 0L);
        assertTrue(s.forward());
        assertEquals(23.2000, s.latitude(), 1e-9);
        List<UpcomingStop> up = path.upcoming(s);
        assertEquals(List.of("B", "C"), up.stream().map(UpcomingStop::name).toList());
        assertTrue(up.get(0).etaSeconds() < up.get(1).etaSeconds());
    }

    @Test
    void positionChangesWithTimeByAboutSpeedTimesElapsed() {
        RoutePath.State a = path.stateAt(0.0, 0L);
        RoutePath.State b = path.stateAt(0.0, 10_000L); // 10 seconds later
        double moved = GeoUtils.haversineMeters(a.latitude(), a.longitude(), b.latitude(), b.longitude());
        assertEquals(10 * RoutePath.SPEED_METERS_PER_SECOND, moved, 1.0);
    }

    @Test
    void busTurnsAroundAtTheLastStopAndHeadsBack() {
        long secondsToEnd = (long) Math.ceil(path.totalMeters() / RoutePath.SPEED_METERS_PER_SECOND) + 5;
        RoutePath.State s = path.stateAt(0.0, secondsToEnd * 1000);
        assertFalse(s.forward());
        List<UpcomingStop> up = path.upcoming(s);
        assertEquals("B", up.get(0).name()); // heading back: next is B, then A
        assertEquals("A", up.get(1).name());
    }

    @Test
    void positionStaysOnTheRoute() {
        for (long t = 0; t < 4_000_000; t += 37_000) {
            RoutePath.State s = path.stateAt(0.5, t);
            assertTrue(s.latitude() >= 23.2000 - 1e-9 && s.latitude() <= 23.2200 + 1e-9);
            assertFalse(Double.isNaN(s.longitude()));
        }
    }

    @Test
    void needsAtLeastTwoStops() {
        assertThrows(IllegalArgumentException.class,
                () -> new RoutePath(List.of(new RoutePath.PathStop(1, "A", 0, 0))));
    }
}
