package com.transitassist.transitassist.service;

import com.transitassist.transitassist.dto.BusResponse;
import com.transitassist.transitassist.dto.CreateStopRequest;
import com.transitassist.transitassist.dto.StopRequestResponse;
import com.transitassist.transitassist.entity.Route;
import com.transitassist.transitassist.entity.Stop;
import com.transitassist.transitassist.entity.StopRequest;
import com.transitassist.transitassist.entity.StopRequestStatus;
import com.transitassist.transitassist.exception.BadRequestException;
import com.transitassist.transitassist.exception.ConflictException;
import com.transitassist.transitassist.exception.ResourceNotFoundException;
import com.transitassist.transitassist.repository.RouteRepository;
import com.transitassist.transitassist.repository.StopRepository;
import com.transitassist.transitassist.repository.StopRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StopRequestService {

    /** The same bus+stop cannot be requested twice within this window. */
    static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(10);

    private final BusSimulator busSimulator;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final StopRequestRepository stopRequestRepository;
    private final Clock clock;

    public StopRequestService(BusSimulator busSimulator, RouteRepository routeRepository,
                              StopRepository stopRepository, StopRequestRepository stopRequestRepository,
                              Clock clock) {
        this.busSimulator = busSimulator;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.stopRequestRepository = stopRequestRepository;
        this.clock = clock;
    }

    @Transactional
    public StopRequestResponse create(CreateStopRequest request) {
        String busCode = request.busCode().trim();
        BusResponse bus = busSimulator.find(busCode)
                .orElseThrow(() -> new BadRequestException("Unknown bus: " + busCode));

        boolean upcoming = bus.upcomingStops().stream().anyMatch(u -> u.stopId() == request.stopId());
        if (!upcoming) {
            throw new BadRequestException(
                    "Stop is not an upcoming stop for this bus (not on its route, or the bus has already passed it)");
        }

        Stop stop = stopRepository.findById(request.stopId())
                .orElseThrow(() -> new BadRequestException("Unknown stop: " + request.stopId()));
        Route route = routeRepository.findById(bus.routeId())
                .orElseThrow(() -> new BadRequestException("Unknown route for bus " + busCode));

        Instant now = clock.instant();
        if (stopRequestRepository.existsByBusCodeAndStopIdAndStatusAndCreatedAtAfter(
                busCode, stop.getId(), StopRequestStatus.PENDING, now.minus(DUPLICATE_WINDOW))) {
            throw new ConflictException("A pending stop request for this bus and stop already exists");
        }

        StopRequest saved = stopRequestRepository.save(
                new StopRequest(busCode, route, stop, StopRequestStatus.PENDING, now));
        return StopRequestResponse.from(saved);
    }

    public StopRequestResponse get(Long id) {
        return stopRequestRepository.findById(id)
                .map(StopRequestResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Stop request " + id + " not found"));
    }

    public List<StopRequestResponse> latest() {
        return stopRequestRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(StopRequestResponse::from).toList();
    }
}
