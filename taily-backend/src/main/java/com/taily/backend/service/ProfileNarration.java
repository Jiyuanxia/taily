package com.taily.backend.service;

import com.taily.backend.dto.profile.AvatarConfigDto;
import com.taily.backend.dto.profile.TravelerArchetypeDto;

public record ProfileNarration(
    String profileSummary, TravelerArchetypeDto travelerArchetype, AvatarConfigDto avatarConfig) {}

