package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record TripRequestDto(@NotBlank String prompt) {}

