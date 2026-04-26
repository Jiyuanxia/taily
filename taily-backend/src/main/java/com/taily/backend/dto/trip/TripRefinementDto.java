package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record TripRefinementDto(
    @NotNull Map<String, Integer> dimensionAdjustments,
    @NotNull String prompt) {}

