package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record ArchetypeDto(@NotBlank String name, @NotBlank String description) {}

