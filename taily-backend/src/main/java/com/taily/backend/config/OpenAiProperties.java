package com.taily.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taily.openai")
public record OpenAiProperties(
    boolean enabled,
    String apiKey,
    String model,
    String imageModel,
    String portraitSize) {
  public OpenAiProperties {
    imageModel = (imageModel == null || imageModel.isBlank()) ? "dall-e-2" : imageModel;
    portraitSize = (portraitSize == null || portraitSize.isBlank()) ? "512x512" : portraitSize;
  }
}

