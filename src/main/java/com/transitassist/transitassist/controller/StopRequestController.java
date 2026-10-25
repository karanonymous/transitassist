package com.transitassist.transitassist.controller;

import com.transitassist.transitassist.dto.CreateStopRequest;
import com.transitassist.transitassist.dto.StopRequestResponse;
import com.transitassist.transitassist.service.StopRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/stop-requests")
public class StopRequestController {

    private final StopRequestService service;

    public StopRequestController(StopRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StopRequestResponse> create(@Valid @RequestBody CreateStopRequest request) {
        StopRequestResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/stop-requests/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public StopRequestResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<StopRequestResponse> latest() {
        return service.latest();
    }
}
