package com.taily.backend.dto.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FitAnalysisDto(
    @NotNull @Valid OverallFitDto overallFit,
    @NotNull List<String> topMatches,
    @NotNull List<String> topMismatches,
    @NotBlank String summary) {}

