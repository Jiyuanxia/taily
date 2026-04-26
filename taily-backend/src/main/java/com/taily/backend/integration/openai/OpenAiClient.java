package com.taily.backend.integration.openai;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.profile.OnboardingAnswersDto;
import com.taily.backend.service.ProfileNarration;
import java.util.Optional;

/**
 * Thin abstraction so we can swap in real OpenAI calls later without changing the /profile/create API.
 */
public interface OpenAiClient {
  Optional<ProfileNarration> generateProfileNarration(
      OnboardingAnswersDto onboardingAnswers, DimensionScoresDto dimensionScores);
}

