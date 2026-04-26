package com.taily.backend.controller;

import com.taily.backend.dto.profile.ProfileCreateRequestDto;
import com.taily.backend.dto.profile.ProfileCreateResponseDto;
import com.taily.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {
  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @PostMapping("/profile/create")
  public ProfileCreateResponseDto createProfile(@Valid @RequestBody ProfileCreateRequestDto request) {
    return profileService.createProfile(request.onboardingAnswers());
  }
}

