package com.taily.backend.dto.trip;

import com.taily.backend.dto.common.DimensionScoresDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * @param profileSummary optional; when present (e.g. from the client session), traveler identity
 *     specs on trip responses use it for the summary line. Otherwise the backend synthesizes one
 *     from scores.
 */
public record UserProfileInputDto(
    @NotNull @Valid DimensionScoresDto dimensionScores, String profileSummary) {}

