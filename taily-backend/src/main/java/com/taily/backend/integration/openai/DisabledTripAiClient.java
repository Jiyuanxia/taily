package com.taily.backend.integration.openai;

import com.taily.backend.dto.trip.TripGenerateWithFitRequestDto;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "taily.openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledTripAiClient implements TripAiClient {
  @Override
  public Optional<TripAiDraft> generateTripDraft(TripGenerateWithFitRequestDto request) {
    return Optional.empty();
  }
}

