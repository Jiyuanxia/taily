package com.taily.backend.dto.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProfileCreateResponseDto(@NotNull @Valid UserProfileDto userProfile) {}

