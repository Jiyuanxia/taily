package com.taily.backend.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record TravelerArchetypeDto(@NotBlank String name, @NotBlank String description) {}

