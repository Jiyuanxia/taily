package com.taily.backend.integration.openai;

import com.taily.backend.dto.trip.ExtractedPreferencesDto;
import com.taily.backend.dto.trip.TripGenerateWithFitRequestDto;
import com.taily.backend.dto.trip.ItineraryDayDto;
import com.taily.backend.dto.trip.WorthConsideringDto;
import com.taily.backend.dto.trip.ArchetypeDto;
import java.util.Optional;
import java.util.List;

/**
 * Lightweight abstraction for future AI-backed trip generation.
 *
 * <p>For the first pass, deterministic generation is used by default. This interface is the swap-in
 * point for real OpenAI integration later.
 */
public interface TripAiClient {
  Optional<TripAiDraft> generateTripDraft(TripGenerateWithFitRequestDto request);

  record TripAiDraft(
      String destination,
      ExtractedPreferencesDto extractedPreferences,
      List<ItineraryDayDto> itinerary,
      String routeSummary,
      String styleSummary,
      List<WorthConsideringDto> worthConsidering,
      String fitSummary,
      ArchetypeDto tripCompanionPet) {}
}

