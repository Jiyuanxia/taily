package com.taily.backend.dto.trip;

import jakarta.validation.Valid;

public record ContextDto(@Valid OriginDto origin) {}

