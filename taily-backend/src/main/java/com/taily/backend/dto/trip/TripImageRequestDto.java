package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TripImageRequestDto(@NotBlank @Size(max = 2500) String prompt) {}

