package com.taily.backend.dto.trip;

import jakarta.validation.constraints.NotBlank;

public record WorthConsideringDto(@NotBlank String name, @NotBlank String reason) {}

