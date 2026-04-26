package com.taily.backend.service;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.identity.TravelerIdentitySpecDto;
import com.taily.backend.dto.profile.OnboardingAnswersDto;
import com.taily.backend.dto.profile.ProfileCreateResponseDto;
import com.taily.backend.dto.profile.UserProfileDto;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
  private final ProfileNormalizationService normalizationService;
  private final ProfileNarrationService narrationService;
  private final IdentitySpecService identitySpecService;

  public ProfileService(
      ProfileNormalizationService normalizationService,
      ProfileNarrationService narrationService,
      IdentitySpecService identitySpecService) {
    this.normalizationService = normalizationService;
    this.narrationService = narrationService;
    this.identitySpecService = identitySpecService;
  }

  public ProfileCreateResponseDto createProfile(OnboardingAnswersDto onboardingAnswers) {
    DimensionScoresDto scores = normalizationService.normalize(onboardingAnswers);
    ProfileNarration narration = narrationService.narrate(onboardingAnswers, scores);

    TravelerIdentitySpecDto travelerIdentitySpec =
        identitySpecService.buildTravelerIdentitySpec(
            scores, narration.profileSummary(), narration.travelerArchetype());

    UserProfileDto userProfile =
        new UserProfileDto(
            scores,
            narration.profileSummary(),
            narration.travelerArchetype(),
            narration.avatarConfig(),
            travelerIdentitySpec);

    return new ProfileCreateResponseDto(userProfile);
  }
}

