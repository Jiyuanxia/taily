package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record TripSummaryDto(
    @NotBlank String origin,
    @NotBlank String destination,
    @NotBlank String durationText,
    @NotBlank String budgetText,
    @NotBlank String routeSummary,
    @NotBlank String styleSummary) {}

