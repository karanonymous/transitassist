package com.transitassist.transitassist.controller;

import com.transitassist.transitassist.dto.RouteResponse;
import com.transitassist.transitassist.service.RouteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteResponse> list() {
        return routeService.listAll();
    }

    @GetMapping("/{id}")
    public RouteResponse get(@PathVariable Long id) {
        return routeService.get(id);
    }
}
