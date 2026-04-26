package com.taily.backend.dto.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProfileCreateRequestDto(@NotNull @Valid OnboardingAnswersDto onboardingAnswers) {}

