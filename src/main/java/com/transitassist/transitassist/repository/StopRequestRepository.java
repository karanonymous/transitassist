package com.transitassist.transitassist.repository;

import com.transitassist.transitassist.entity.StopRequest;
import com.transitassist.transitassist.entity.StopRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StopRequestRepository extends JpaRepository<StopRequest, Long> {

    @EntityGraph(attributePaths = {"route", "stop"})
    List<StopRequest> findTop10ByOrderByCreatedAtDesc();

    boolean existsByBusCodeAndStopIdAndStatusAndCreatedAtAfter(
            String busCode, Long stopId, StopRequestStatus status, Instant after);
}
