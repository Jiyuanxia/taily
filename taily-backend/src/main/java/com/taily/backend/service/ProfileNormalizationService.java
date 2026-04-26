package com.taily.backend.service;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.profile.OnboardingAnswersDto;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ProfileNormalizationService {

  public DimensionScoresDto normalize(OnboardingAnswersDto a) {
    return new DimensionScoresDto(
        normalizeBudget(a.budget()),
        normalizePace(a.pace()),
        normalizeGeneric(a.foodFocus()),
        normalizeGeneric(a.sightseeing()),
        normalizeGeneric(a.comfort()),
        normalizeGeneric(a.exploration()));
  }

  private int normalizeBudget(String raw) {
    String v = norm(raw);
    return switch (v) {
      case "low", "cheap", "budget" -> 1;
      case "balanced", "mid", "mid-range", "midrange", "medium" -> 3;
      case "high", "luxury", "splurge" -> 5;
      default -> 3;
    };
  }

  private int normalizePace(String raw) {
    String v = norm(raw);
    return switch (v) {
      case "relaxed", "slow" -> 2; // aligns with example in DATA_CONTRACTS
      case "balanced", "medium" -> 3;
      case "fast", "busy" -> 4;
      case "packed", "intense" -> 5;
      default -> 3;
    };
  }

  private int normalizeGeneric(String raw) {
    String v = norm(raw);
    return switch (v) {
      case "low" -> 1;
      case "low-medium", "lowmedium", "low_med", "lowmed" -> 2;
      case "medium" -> 3;
      case "medium-high", "mediumhigh", "med-high", "medhigh" -> 4;
      case "high" -> 5;
      default -> 3;
    };
  }

  private static String norm(String raw) {
    if (raw == null) return "";
    return raw.trim().toLowerCase(Locale.ROOT).replace('—', '-').replace('–', '-');
  }
}

