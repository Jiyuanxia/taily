package com.taily.backend.dto.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CurrentTripInputDto(
    @NotNull @Valid TripSummaryDto tripSummary,
    @NotNull @Valid List<ItineraryDayDto> itinerary,
    @NotNull @Valid TripProfileDto tripProfile) {}

