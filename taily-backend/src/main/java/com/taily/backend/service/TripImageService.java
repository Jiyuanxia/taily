package com.taily.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taily.backend.config.OpenAiProperties;
import com.taily.backend.dto.trip.TripImageRequestDto;
import com.taily.backend.dto.trip.TripImageResponseDto;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TripImageService {
  private static final URI IMAGES_URI = URI.create("https://api.openai.com/v1/images/generations");
  private static final int DALLE2_PROMPT_MAX = 1000;
  private static final Logger log = LoggerFactory.getLogger(TripImageService.class);

  private final OpenAiProperties props;
  private final ObjectMapper objectMapper;
  private final HttpClient http;

  public TripImageService(OpenAiProperties props, ObjectMapper objectMapper) {
    this.props = props;
    this.objectMapper = objectMapper;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  public TripImageResponseDto generateTripImage(TripImageRequestDto request) {
    String prompt = truncatePrompt(request.prompt());
    String specHash = sha256Hex("trip_image|v1|" + prompt);

    if (!props.enabled()) {
      log.info("Trip image generation disabled (taily.openai.enabled=false). specHash={}", specHash);
      return TripImageResponseDto.fail(specHash, "OpenAI integration is disabled.");
    }
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      log.info(
          "Trip image generation blocked (missing api key). specHash={} hint=Set env OPENAI_API_KEY or create taily-backend/.env with OPENAI_API_KEY=...",
          specHash);
      return TripImageResponseDto.fail(specHash, "OpenAI API key is not configured.");
    }
    if (prompt.isBlank()) {
      log.info("Trip image generation blocked (empty prompt). specHash={}", specHash);
      return TripImageResponseDto.fail(specHash, "Prompt was empty after trimming.");
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
            "Trip image generation failed (OpenAI HTTP {}). specHash={} model={}",
            res.statusCode(),
            specHash,
            props.imageModel());
        return TripImageResponseDto.fail(
            specHash, "Image API returned HTTP " + res.statusCode() + ". " + abbreviate(res.body(), 200));
      }

      JsonNode root = objectMapper.readTree(res.body());
      JsonNode data0 = root.path("data").path(0);
      String b64 = data0.path("b64_json").asText(null);
      if (b64 == null || b64.isBlank()) {
        JsonNode err = root.path("error").path("message");
        String msg = err.isMissingNode() || err.asText().isBlank() ? "Missing b64_json in response." : err.asText();
        log.info(
            "Trip image generation failed (missing b64_json). specHash={} model={} msg={}",
            specHash,
            props.imageModel(),
            abbreviate(msg, 200));
        return TripImageResponseDto.fail(specHash, msg);
      }

      return TripImageResponseDto.ok(specHash, "image/png", b64);
    } catch (Exception e) {
      log.info("Trip image generation exception. specHash={} err={}", specHash, abbreviate(e.getMessage(), 240));
      return TripImageResponseDto.fail(
          specHash, "Trip image generation failed: " + abbreviate(e.getMessage(), 240));
    }
  }

  private static String truncatePrompt(String prompt) {
    String p = prompt == null ? "" : prompt.trim();
    if (p.length() <= DALLE2_PROMPT_MAX) return p;
    return p.substring(0, DALLE2_PROMPT_MAX);
  }

  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(d);
    } catch (Exception e) {
      // Should never happen; fall back to a stable non-empty value.
      return "0000000000000000000000000000000000000000000000000000000000000000";
    }
  }

  private static String abbreviate(String s, int max) {
    if (s == null) return "";
    String t = s.replace("\n", " ").trim();
    if (t.length() <= max) return t;
    return t.substring(0, max) + "…";
  }
}

