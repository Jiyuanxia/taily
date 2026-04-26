package com.taily.backend.dto.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TravelerIdentitySpecDto(
    @NotBlank String kind,
    @NotBlank String archetypeId,
    @NotBlank String archetypeName,
    @NotBlank String archetypeDescription,
    @NotNull @NotEmpty List<String> moodTags,
    @NotBlank String summary,
    @NotNull @Valid DimensionLevelsDto dimensionLevels,
    @NotNull @NotEmpty List<@Valid IdentityVisualSlotDto> visualSlots,
    @NotNull @Valid IdentityBackgroundDto background,
    @NotNull @NotEmpty List<String> visualTags,
    @NotBlank String imagePrompt,
    @NotBlank String specHash) {}
