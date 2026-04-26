package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record OriginDto(@NotBlank String value, String source) {}

