package com.taily.backend.dto.identity;

import jakarta.validation.constraints.NotBlank;

public record IdentityBackgroundDto(
    @NotBlank String setting, @NotBlank String elements, @NotBlank String colorMood) {}
