package com.transitassist.transitassist.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StopRequestApiTest extends AbstractApiTest {

    /** The bus/stop pair with the longest simulated wait, so the bus cannot pass the stop mid-test. */
    private record Target(String busCode, long stopId) {
    }

    private Target farthestUpcoming() throws Exception {
        String json = mvc.perform(get("/api/buses")).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> buses = JsonPath.read(json, "$");
        Target best = null;
        int bestEta = -1;
        for (Map<String, Object> bus : buses) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> upcoming = (List<Map<String, Object>>) bus.get("upcomingStops");
            for (Map<String, Object> stop : upcoming) {
                int eta = ((Number) stop.get("etaSeconds")).intValue();
                if (eta > bestEta) {
                    bestEta = eta;
                    best = new Target((String) bus.get("code"), ((Number) stop.get("stopId")).longValue());
                }
            }
        }
        assertTrue(bestEta > 30, "expected a bus with a stop more than 30 s away");
        return best;
    }

    private String body(String busCode, long stopId) {
        return "{\"busCode\":\"" + busCode + "\",\"stopId\":" + stopId + "}";
    }

    @Test
    void createsPendingRequestAndReadsItBack() throws Exception {
        Target t = farthestUpcoming();
        String created = mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON)
                        .content(body(t.busCode(), t.stopId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.notice").isString())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(created, "$.id");

        mvc.perform(get("/api/stop-requests/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.busCode").value(t.busCode()));
        mvc.perform(get("/api/stop-requests")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())));
    }

    @Test
    void duplicatePendingRequestReturns409() throws Exception {
        Target t = farthestUpcoming();
        mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON)
                .content(body(t.busCode(), t.stopId()))).andExpect(status().isCreated());
        mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON)
                .content(body(t.busCode(), t.stopId()))).andExpect(status().isConflict());
    }

    @Test
    void invalidRequestsReturn400() throws Exception {
        // blank body fields -> validation errors with per-field messages
        mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.busCode").exists())
                .andExpect(jsonPath("$.fieldErrors.stopId").exists());
        // unknown bus
        mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON).content(body("999-Z", 1)))
                .andExpect(status().isBadRequest());
        // stop that is not on the bus's route: "Bhopal Junction" is not on route 202
        String stops = mvc.perform(get("/api/stops")).andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(stops, "$[?(@.name=='Bhopal Junction')].id");
        mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON)
                        .content(body("202-A", ids.get(0).longValue())))
                .andExpect(status().isBadRequest());
        // malformed JSON
        mvc.perform(post("/api/stop-requests").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownRequestIdReturns404() throws Exception {
        mvc.perform(get("/api/stop-requests/999999")).andExpect(status().isNotFound());
    }
}
