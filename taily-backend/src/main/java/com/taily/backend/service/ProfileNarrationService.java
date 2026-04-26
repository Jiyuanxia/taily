package com.taily.backend.service;

import com.taily.backend.config.OpenAiProperties;
import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.profile.AvatarConfigDto;
import com.taily.backend.dto.profile.OnboardingAnswersDto;
import com.taily.backend.dto.profile.TravelerArchetypeDto;
import com.taily.backend.integration.openai.OpenAiClient;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProfileNarrationService {
  private final OpenAiClient openAiClient;
  private final OpenAiProperties openAiProperties;
  private final IdentitySpecService identitySpecService;

  public ProfileNarrationService(
      OpenAiClient openAiClient,
      OpenAiProperties openAiProperties,
      IdentitySpecService identitySpecService) {
    this.openAiClient = openAiClient;
    this.openAiProperties = openAiProperties;
    this.identitySpecService = identitySpecService;
  }

  public ProfileNarration narrate(OnboardingAnswersDto answers, DimensionScoresDto scores) {
    if (openAiProperties.enabled()) {
      Optional<ProfileNarration> ai = openAiClient.generateProfileNarration(answers, scores);
      if (ai.isPresent()) {
        ProfileNarration n = ai.get();
        TravelerArchetypeDto canon = identitySpecService.pickTravelerArchetype(scores);
        return new ProfileNarration(n.profileSummary(), canon, n.avatarConfig());
      }
    }
    return deterministicFallback(scores);
  }

  private ProfileNarration deterministicFallback(DimensionScoresDto s) {
    String summary = identitySpecService.travelerSummaryFromScores(s);
    TravelerArchetypeDto archetype = identitySpecService.pickTravelerArchetype(s);
    AvatarConfigDto avatar = buildAvatarConfig(s);
    return new ProfileNarration(summary, archetype, avatar);
  }

  private static AvatarConfigDto buildAvatarConfig(DimensionScoresDto s) {
    String styleTone = (s.comfort() >= 4) ? "warm" : "neutral";
    String energyLevel = (s.pace() <= 2) ? "low-medium" : (s.pace() >= 4) ? "high" : "medium";
    String vibe;
    if (s.foodFocus() >= 4 && s.pace() <= 3) {
      vibe = "calm and refined";
    } else if (s.exploration() >= 4) {
      vibe = "curious and open";
    } else if (s.sightseeing() >= 4) {
      vibe = "focused and upbeat";
    } else {
      vibe = "calm and thoughtful";
    }
    return new AvatarConfigDto(styleTone, energyLevel, vibe);
  }
}

