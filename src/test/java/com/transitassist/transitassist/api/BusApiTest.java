package com.transitassist.transitassist.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusApiTest extends AbstractApiTest {

    @Test
    void listsFourSimulatedBusesWithOccupancyAndEta() throws Exception {
        mvc.perform(get("/api/buses")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].simulated").value(true))
                .andExpect(jsonPath("$[0].occupancy").isString())
                .andExpect(jsonPath("$[0].upcomingStops").isArray());
    }

    @Test
    void busPositionsChangeOverTime() throws Exception {
        String first = mvc.perform(get("/api/buses")).andReturn().getResponse().getContentAsString();
        Thread.sleep(1500);
        String second = mvc.perform(get("/api/buses")).andReturn().getResponse().getContentAsString();
        Double lat1 = JsonPath.read(first, "$[0].latitude");
        Double lat2 = JsonPath.read(second, "$[0].latitude");
        Double lng1 = JsonPath.read(first, "$[0].longitude");
        Double lng2 = JsonPath.read(second, "$[0].longitude");
        assertNotEquals(lat1 + "," + lng1, lat2 + "," + lng2);
    }
}
