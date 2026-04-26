package com.taily.backend.dto.identity;

import jakarta.validation.constraints.NotBlank;

/**
 * Six-dimension levels for identity visuals: {@code low}, {@code medium}, or {@code high} per
 * score band (1–2, 3, 4–5).
 */
public record DimensionLevelsDto(
    @NotBlank String budget,
    @NotBlank String pace,
    @NotBlank String foodFocus,
    @NotBlank String sightseeing,
    @NotBlank String comfort,
    @NotBlank String exploration) {}
