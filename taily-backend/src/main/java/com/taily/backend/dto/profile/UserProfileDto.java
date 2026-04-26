package com.taily.backend.dto.profile;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.identity.TravelerIdentitySpecDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserProfileDto(
    @NotNull @Valid DimensionScoresDto dimensionScores,
    @NotBlank String profileSummary,
    @NotNull @Valid TravelerArchetypeDto travelerArchetype,
    @NotNull @Valid AvatarConfigDto avatarConfig,
    @NotNull @Valid TravelerIdentitySpecDto travelerIdentitySpec) {}

