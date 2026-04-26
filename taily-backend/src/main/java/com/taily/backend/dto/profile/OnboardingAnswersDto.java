package com.taily.backend.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record OnboardingAnswersDto(
    @NotBlank String budget,
    @NotBlank String pace,
    @NotBlank String foodFocus,
    @NotBlank String sightseeing,
    @NotBlank String comfort,
    @NotBlank String exploration) {}

