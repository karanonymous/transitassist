package com.transitassist.transitassist.controller;

import com.transitassist.transitassist.dto.NearbyStopResponse;
import com.transitassist.transitassist.dto.StopResponse;
import com.transitassist.transitassist.service.StopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
public class StopController {

    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping
    public List<StopResponse> list() {
        return stopService.listAll();
    }

    @GetMapping("/nearby")
    public List<NearbyStopResponse> nearby(@RequestParam double lat, @RequestParam double lng,
                                           @RequestParam(defaultValue = "1000") int radius) {
        return stopService.nearby(lat, lng, radius);
    }
}
