package com.taily.backend.dto.identity;

public record IdentityPortraitResponseDto(
    boolean success,
    String mimeType,
    String base64,
    String errorMessage) {

  public static IdentityPortraitResponseDto ok(String mimeType, String base64) {
    return new IdentityPortraitResponseDto(true, mimeType, base64, null);
  }

  public static IdentityPortraitResponseDto fail(String errorMessage) {
    return new IdentityPortraitResponseDto(false, null, null, errorMessage);
  }
}
