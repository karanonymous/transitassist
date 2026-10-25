package com.transitassist.transitassist.controller;

import com.transitassist.transitassist.dto.BusResponse;
import com.transitassist.transitassist.service.BusSimulator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusSimulator busSimulator;

    public BusController(BusSimulator busSimulator) {
        this.busSimulator = busSimulator;
    }

    /** SIMULATED bus positions. Not real GPS data. */
    @GetMapping
    public List<BusResponse> list() {
        return busSimulator.allBuses();
    }
}
