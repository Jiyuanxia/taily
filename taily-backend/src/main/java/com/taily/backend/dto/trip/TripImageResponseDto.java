package com.taily.backend.dto.trip;

public record TripImageResponseDto(
    boolean success, String specHash, String mimeType, String base64, String errorMessage) {
  public static TripImageResponseDto ok(String specHash, String mimeType, String base64) {
    return new TripImageResponseDto(true, specHash, mimeType, base64, null);
  }

  public static TripImageResponseDto fail(String specHash, String errorMessage) {
    return new TripImageResponseDto(false, specHash, null, null, errorMessage);
  }
}

