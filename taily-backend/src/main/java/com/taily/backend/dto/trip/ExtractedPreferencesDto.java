package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ExtractedPreferencesDto(
    @NotNull List<String> mustPassStops,
    @NotNull List<String> mustEat,
    @NotNull List<String> mustSeeOrDo,
    @NotBlank String lodgingPreference,
    @NotBlank String budgetPreference,
    @NotBlank String pacePreference) {}

