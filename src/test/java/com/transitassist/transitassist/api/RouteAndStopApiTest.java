package com.transitassist.transitassist.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RouteAndStopApiTest extends AbstractApiTest {

    @Test
    void routesComeWithOrderedStops() throws Exception {
        mvc.perform(get("/api/routes")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].routeNumber").value("101"))
                .andExpect(jsonPath("$[0].stops[0].name").value("Bhopal Junction"))
                .andExpect(jsonPath("$[0].stops[4].name").value("MP Nagar Zone-1"))
                .andExpect(jsonPath("$[1].stops", hasSize(3)));
    }

    @Test
    void missingRouteReturns404() throws Exception {
        mvc.perform(get("/api/routes/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void nearbyFindsStopsInsideTheRadiusSortedByDistance() throws Exception {
        mvc.perform(get("/api/stops/nearby?lat=23.2290&lng=77.4200&radius=500")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Board Office Square"))
                .andExpect(jsonPath("$[0].distanceMeters").value(lessThan(10)));
    }

    @Test
    void nearbyRejectsBadInput() throws Exception {
        mvc.perform(get("/api/stops/nearby?lat=200&lng=77.4")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/stops/nearby?lat=23.2&lng=77.4&radius=0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/stops/nearby?lat=abc&lng=77.4")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/stops/nearby?lat=23.2")).andExpect(status().isBadRequest());
    }
}
