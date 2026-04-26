package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ItineraryDayDto(
    int day,
    @NotBlank String title,
    @NotBlank String area,
    @NotNull List<String> activities,
    @NotBlank String vibeNote,
    @NotNull List<String> mustHaveCoverage,
    @NotNull List<OptionalHighlightDto> optionalHighlights) {}

