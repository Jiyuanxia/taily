package com.taily.backend.dto.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripRefineRequestDto(
    @NotNull @Valid UserProfileInputDto userProfile,
    @NotNull @Valid CurrentTripInputDto currentTrip,
    @NotNull @Valid TripRefinementDto refinement) {}

