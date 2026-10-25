package com.transitassist.transitassist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStopRequest(
        @NotBlank(message = "busCode is required") @Size(max = 40, message = "busCode must be at most 40 characters") String busCode,
        @NotNull(message = "stopId is required") Long stopId) {
}
