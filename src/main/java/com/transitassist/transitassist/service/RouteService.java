package com.transitassist.transitassist.service;

import com.transitassist.transitassist.dto.RouteResponse;
import com.transitassist.transitassist.dto.StopResponse;
import com.transitassist.transitassist.entity.Route;
import com.transitassist.transitassist.entity.RouteStop;
import com.transitassist.transitassist.exception.ResourceNotFoundException;
import com.transitassist.transitassist.repository.RouteRepository;
import com.transitassist.transitassist.repository.RouteStopRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    public RouteService(RouteRepository routeRepository, RouteStopRepository routeStopRepository) {
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
    }

    public List<RouteResponse> listAll() {
        Map<Long, List<StopResponse>> stopsByRoute = stopsByRoute();
        return routeRepository.findAll(Sort.by("routeNumber")).stream()
                .map(r -> RouteResponse.from(r, stopsByRoute.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    public RouteResponse get(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route " + id + " not found"));
        return RouteResponse.from(route, stopsByRoute().getOrDefault(id, List.of()));
    }

    private Map<Long, List<StopResponse>> stopsByRoute() {
        Map<Long, List<StopResponse>> map = new HashMap<>();
        for (RouteStop rs : routeStopRepository.findAllOrdered()) {
            map.computeIfAbsent(rs.getRoute().getId(), k -> new ArrayList<>()).add(StopResponse.from(rs.getStop()));
        }
        return map;
    }
}
