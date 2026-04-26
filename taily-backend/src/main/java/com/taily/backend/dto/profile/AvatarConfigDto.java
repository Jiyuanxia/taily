package com.taily.backend.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record AvatarConfigDto(
    @NotBlank String styleTone, @NotBlank String energyLevel, @NotBlank String vibe) {}

