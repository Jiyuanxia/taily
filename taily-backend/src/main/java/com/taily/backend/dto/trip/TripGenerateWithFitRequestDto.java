package com.taily.backend.dto.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripGenerateWithFitRequestDto(
    @NotNull @Valid UserProfileInputDto userProfile,
    @NotNull @Valid TripRequestDto tripRequest,
    @Valid ContextDto context) {}

