package com.taily.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taily.backend.config.OpenAiProperties;
import com.taily.backend.dto.identity.IdentityPortraitRequestDto;
import com.taily.backend.dto.identity.IdentityPortraitResponseDto;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IdentityPortraitService {
  private static final URI IMAGES_URI = URI.create("https://api.openai.com/v1/images/generations");
  private static final int DALLE2_PROMPT_MAX = 1000;
  private static final Logger log = LoggerFactory.getLogger(IdentityPortraitService.class);

  private final OpenAiProperties props;
  private final ObjectMapper objectMapper;
  private final HttpClient http;

  public IdentityPortraitService(OpenAiProperties props, ObjectMapper objectMapper) {
    this.props = props;
    this.objectMapper = objectMapper;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  public IdentityPortraitResponseDto generatePortrait(IdentityPortraitRequestDto request) {
    if (!props.enabled()) {
      log.info("Portrait generation disabled (taily.openai.enabled=false). kind={} specHash={}", request.kind(), request.specHash());
      return IdentityPortraitResponseDto.fail("OpenAI integration is disabled.");
    }
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      log.info(
          "Portrait generation blocked (missing api key). kind={} specHash={} hint=Set env OPENAI_API_KEY or create taily-backend/.env with OPENAI_API_KEY=...",
          request.kind(),
          request.specHash());
      return IdentityPortraitResponseDto.fail("OpenAI API key is not configured.");
    }

    String prompt = truncatePrompt(request.prompt());
    if (prompt.isBlank()) {
      log.info("Portrait generation blocked (empty prompt). kind={} specHash={}", request.kind(), request.specHash());
      return IdentityPortraitResponseDto.fail("Prompt was empty after trimming.");
    }

    try {
      Map<String, Object> body =
          Map.of(
              "model",
              props.imageModel(),
              "prompt",
              prompt,
              "size",
              props.portraitSize(),
              "n",
              1,
              "response_format",
              "b64_json");

      String json = objectMapper.writeValueAsString(body);
      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(IMAGES_URI)
              .timeout(Duration.ofSeconds(120))
              .header("Authorization", "Bearer " + props.apiKey())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();

      HttpResponse<String> res = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() < 200 || res.statusCode() >= 300) {
        log.info(
            "Portrait generation failed (OpenAI HTTP {}). kind={} specHash={} model={}",
            res.statusCode(),
            request.kind(),
            request.specHash(),
            props.imageModel());
        return IdentityPortraitResponseDto.fail(
            "Image API returned HTTP " + res.statusCode() + ". " + abbreviate(res.body(), 200));
      }

      JsonNode root = objectMapper.readTree(res.body());
      JsonNode data0 = root.path("data").path(0);
      String b64 = data0.path("b64_json").asText(null);
      if (b64 == null || b64.isBlank()) {
        JsonNode err = root.path("error").path("message");
        String msg = err.isMissingNode() || err.asText().isBlank() ? "Missing b64_json in response." : err.asText();
        log.info(
            "Portrait generation failed (missing b64_json). kind={} specHash={} model={} msg={}",
            request.kind(),
            request.specHash(),
            props.imageModel(),
            abbreviate(msg, 200));
        return IdentityPortraitResponseDto.fail(msg);
      }

      return IdentityPortraitResponseDto.ok("image/png", b64);
    } catch (Exception e) {
      log.info(
          "Portrait generation exception. kind={} specHash={} model={} err={}",
          request.kind(),
          request.specHash(),
          props.imageModel(),
          abbreviate(e.getMessage(), 240));
      return IdentityPortraitResponseDto.fail(
          "Portrait generation failed: " + abbreviate(e.getMessage(), 240));
    }
  }

  private static String truncatePrompt(String prompt) {
    String p = prompt == null ? "" : prompt.trim();
    if (p.length() <= DALLE2_PROMPT_MAX) return p;
    return p.substring(0, DALLE2_PROMPT_MAX);
  }

  private static String abbreviate(String s, int max) {
    if (s == null) return "";
    String t = s.replace("\n", " ").trim();
    if (t.length() <= max) return t;
    return t.substring(0, max) + "…";
  }
}
