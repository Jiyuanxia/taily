package com.taily.backend.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record DimensionScoresDto(
    @Min(1) @Max(5) int budget,
    @Min(1) @Max(5) int pace,
    @Min(1) @Max(5) int foodFocus,
    @Min(1) @Max(5) int sightseeing,
    @Min(1) @Max(5) int comfort,
    @Min(1) @Max(5) int exploration) {}

