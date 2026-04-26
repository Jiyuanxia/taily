package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record OptionalHighlightDto(@NotBlank String name, @NotBlank String reason) {}

