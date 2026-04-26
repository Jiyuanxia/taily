package com.taily.backend.dto.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IdentityPortraitRequestDto(
    @NotBlank
        @Pattern(
            regexp = "traveler|trip_companion_pet",
            message = "kind must be traveler or trip_companion_pet")
        String kind,
    @NotBlank @Size(max = 8000) String prompt,
    @NotBlank @Size(max = 128) String specHash) {}
