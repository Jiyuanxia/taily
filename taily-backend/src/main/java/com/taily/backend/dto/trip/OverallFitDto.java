package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record OverallFitDto(int score, @NotBlank String label) {}

