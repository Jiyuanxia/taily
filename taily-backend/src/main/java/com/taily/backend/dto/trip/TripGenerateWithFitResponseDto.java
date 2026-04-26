package com.taily.backend.dto.trip;

import com.taily.backend.dto.identity.TravelerIdentitySpecDto;
import com.taily.backend.dto.identity.TripCompanionSpecDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TripGenerateWithFitResponseDto(
    @NotNull @Valid TripSummaryDto tripSummary,
    @NotNull @Valid ExtractedPreferencesDto extractedPreferences,
    @NotNull @Valid List<ItineraryDayDto> itinerary,
    @NotNull @Valid List<WorthConsideringDto> worthConsidering,
    @NotNull @Valid TripProfileDto tripProfile,
    @NotNull @Valid FitAnalysisDto fitAnalysis,
    @NotNull @Valid ArchetypeDto travelerArchetype,
    @NotNull @Valid ArchetypeDto tripCompanionPet,
    @NotNull @Valid TravelerIdentitySpecDto travelerIdentitySpec,
    @NotNull @Valid TripCompanionSpecDto tripCompanionSpec) {}

