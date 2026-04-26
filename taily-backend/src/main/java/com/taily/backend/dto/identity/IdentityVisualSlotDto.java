package com.taily.backend.dto.identity;

import jakarta.validation.constraints.NotBlank;

public record IdentityVisualSlotDto(
    @NotBlank String slot,
    @NotBlank String dimension,
    @NotBlank String level,
    @NotBlank String descriptor) {}
