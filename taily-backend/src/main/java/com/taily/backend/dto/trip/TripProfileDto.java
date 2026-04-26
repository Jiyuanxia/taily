package com.taily.backend.dto.trip;

import com.taily.backend.dto.common.DimensionScoresDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripProfileDto(@NotNull @Valid DimensionScoresDto dimensionScores) {}

