package com.taily.backend.integration.openai;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.profile.OnboardingAnswersDto;
import com.taily.backend.service.ProfileNarration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DisabledOpenAiClient implements OpenAiClient {
  @Override
  public Optional<ProfileNarration> generateProfileNarration(
      OnboardingAnswersDto onboardingAnswers, DimensionScoresDto dimensionScores) {
    return Optional.empty();
  }
}

