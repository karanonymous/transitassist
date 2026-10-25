package com.transitassist.transitassist.dto;

/** A stop the bus has not reached yet, with a simulated ETA in seconds. */
public record UpcomingStop(long stopId, String name, int etaSeconds) {
}
