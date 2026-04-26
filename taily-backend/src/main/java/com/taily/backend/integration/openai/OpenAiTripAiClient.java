package com.taily.backend.integration.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taily.backend.config.OpenAiProperties;
import com.taily.backend.dto.trip.ArchetypeDto;
import com.taily.backend.dto.trip.ExtractedPreferencesDto;
import com.taily.backend.dto.trip.ItineraryDayDto;
import com.taily.backend.dto.trip.OptionalHighlightDto;
import com.taily.backend.dto.trip.TripGenerateWithFitRequestDto;
import com.taily.backend.dto.trip.WorthConsideringDto;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "taily.openai", name = "enabled", havingValue = "true")
public class OpenAiTripAiClient implements TripAiClient {
  private static final URI CHAT_COMPLETIONS_URI = URI.create("https://api.openai.com/v1/chat/completions");

  private final OpenAiProperties props;
  private final ObjectMapper objectMapper;
  private final HttpClient http;

  public OpenAiTripAiClient(OpenAiProperties props, ObjectMapper objectMapper) {
    this.props = props;
    this.objectMapper = objectMapper;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  @Override
  public Optional<TripAiDraft> generateTripDraft(TripGenerateWithFitRequestDto request) {
    if (props.apiKey() == null || props.apiKey().isBlank()) return Optional.empty();

    try {
      String prompt = request.tripRequest().prompt();
      List<Map<String, Object>> messages = new ArrayList<>();
      messages.add(Map.of("role", "system", "content", systemPrompt()));
      messages.add(
          Map.of("role", "user", "content", userPrompt(prompt, request.userProfile().dimensionScores())));

      Map<String, Object> payload =
          Map.of("model", props.model(), "temperature", 0.7, "messages", messages);

      String body = objectMapper.writeValueAsString(payload);
      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(CHAT_COMPLETIONS_URI)
              .timeout(Duration.ofSeconds(35))
              .header("Authorization", "Bearer " + props.apiKey())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();

      HttpResponse<String> res = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() < 200 || res.statusCode() >= 300) return Optional.empty();

      String content = extractAssistantContent(res.body());
      if (content == null || content.isBlank()) return Optional.empty();

      TripAiJson json = parseJson(content);
      if (json == null) return Optional.empty();

      TripAiDraft draft = toDraft(json);
      if (!isValidDraft(draft)) return Optional.empty();

      return Optional.of(normalizeDraft(draft));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private String systemPrompt() {
    return """
You are Taily, an AI travel planner.

Return ONLY valid JSON for the schema described below. No prose, no markdown.

Goal: Produce a realistic MVP trip package that feels personal, calm, and product-real.
Primary requirement: Preserve explicit user constraints from the prompt whenever they are clearly stated.
Constraints:
- Keep it lightweight and readable.
- Use 5 days by default unless the prompt explicitly states a duration (e.g. "4-day", "4 days", "for 4 days"). If duration is explicit, match it.
- Activities must be short phrases, not paragraphs.
- Keep summaries to 1 sentence each.
- Do not invent extreme claims or unsafe advice.
- If the user clearly specifies an origin/destination, route order, or pass-through stops, reflect them in:
  - extractedPreferences.mustPassStops (in route order)
  - routeSummary (in route order, using the same stop names)
  - itinerary areas/activities (honor those stops as much as possible)
- If the user specifies "must eat" items, list them under mustEat and include at least one in the itinerary.
- If the user specifies lodging/budget/pace, copy that intent into the relevant extractedPreferences fields.

JSON schema:
{
  "destination": "string (city/region name)",
  "routeSummary": "string",
  "styleSummary": "string",
  "extractedPreferences": {
    "mustPassStops": ["string"],
    "mustEat": ["string"],
    "mustSeeOrDo": ["string"],
    "lodgingPreference": "string",
    "budgetPreference": "string",
    "pacePreference": "string"
  },
  "itinerary": [
    {
      "day": 1,
      "title": "string",
      "area": "string",
      "activities": ["string"],
      "vibeNote": "string",
      "mustHaveCoverage": ["string"],
      "optionalHighlights": [{"name":"string","reason":"string"}]
    }
  ],
  "worthConsidering": [{"name":"string","reason":"string"}],
  "fitSummary": "string",
  "tripCompanionPet": {"name":"string","description":"string"}
}
""";
  }

  private String userPrompt(String prompt, Object userDimensionScores) {
    return """
Trip request prompt:
"""
        + prompt
        + """

User dimension scores (1-5):
"""
        + userDimensionScores
        + """

Generate the JSON now.
""";
  }

  private String extractAssistantContent(String responseBody) throws Exception {
    Map<String, Object> root =
        objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
    Object choicesObj = root.get("choices");
    if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) return null;
    Object first = choices.getFirst();
    if (!(first instanceof Map<?, ?> firstMap)) return null;
    Object messageObj = firstMap.get("message");
    if (!(messageObj instanceof Map<?, ?> msg)) return null;
    Object content = msg.get("content");
    return (content instanceof String s) ? s : null;
  }

  private TripAiJson parseJson(String content) {
    String trimmed = content.trim();
    // Some models may wrap JSON in extra whitespace; keep it strict but tolerant.
    int start = trimmed.indexOf('{');
    int end = trimmed.lastIndexOf('}');
    if (start < 0 || end < 0 || end <= start) return null;
    String json = trimmed.substring(start, end + 1);
    try {
      return objectMapper.readValue(json, TripAiJson.class);
    } catch (Exception e) {
      return null;
    }
  }

  private TripAiDraft toDraft(TripAiJson json) {
    ExtractedPreferencesDto prefs =
        new ExtractedPreferencesDto(
            safeList(json.extractedPreferences.mustPassStops),
            safeList(json.extractedPreferences.mustEat),
            safeList(json.extractedPreferences.mustSeeOrDo),
            safeStr(json.extractedPreferences.lodgingPreference, "Comfortable hotel"),
            safeStr(json.extractedPreferences.budgetPreference, "Balanced / mid-range"),
            safeStr(json.extractedPreferences.pacePreference, "Balanced"));

    List<ItineraryDayDto> itinerary = new ArrayList<>();
    for (TripAiJson.Day d : safeList(json.itinerary)) {
      itinerary.add(
          new ItineraryDayDto(
              d.day,
              safeStr(d.title, "Day plan"),
              safeStr(d.area, safeStr(json.destination, "Destination")),
              safeList(d.activities),
              safeStr(d.vibeNote, "A calm, steady day"),
              safeList(d.mustHaveCoverage),
              toHighlights(d.optionalHighlights)));
    }

    List<WorthConsideringDto> worth = new ArrayList<>();
    for (TripAiJson.NamedReason x : safeList(json.worthConsidering)) {
      String name = safeStr(x.name, null);
      String reason = safeStr(x.reason, null);
      if (name != null && reason != null) worth.add(new WorthConsideringDto(name, reason));
    }

    ArchetypeDto pet =
        new ArchetypeDto(
            safeStr(json.tripCompanionPet.name, "Curious Cat"),
            safeStr(json.tripCompanionPet.description, "A calm companion with a taste for small discoveries."));

    return new TripAiDraft(
        safeStr(json.destination, "Your destination"),
        prefs,
        itinerary,
        safeStr(json.routeSummary, "A trip shaped around your request"),
        safeStr(json.styleSummary, "A trip shaped to your travel style"),
        worth,
        safeStr(json.fitSummary, "This trip matches you well overall, with one or two small tradeoffs."),
        pet);
  }

  private boolean isValidDraft(TripAiDraft d) {
    if (d == null) return false;
    if (d.destination() == null || d.destination().isBlank()) return false;
    if (d.extractedPreferences() == null) return false;
    if (d.itinerary() == null || d.itinerary().size() < 3) return false;
    if (d.routeSummary() == null || d.routeSummary().isBlank()) return false;
    if (d.styleSummary() == null || d.styleSummary().isBlank()) return false;
    if (d.fitSummary() == null || d.fitSummary().isBlank()) return false;
    if (d.tripCompanionPet() == null || d.tripCompanionPet().name() == null || d.tripCompanionPet().name().isBlank())
      return false;

    // Validate itinerary days: sequential 1..N and activities non-empty.
    int n = d.itinerary().size();
    for (int i = 0; i < n; i++) {
      ItineraryDayDto day = d.itinerary().get(i);
      if (day.day() != i + 1) return false;
      if (day.activities() == null || day.activities().isEmpty()) return false;
      if (day.title() == null || day.title().isBlank()) return false;
      if (day.area() == null || day.area().isBlank()) return false;
      if (day.vibeNote() == null || day.vibeNote().isBlank()) return false;
    }
    return true;
  }

  private TripAiDraft normalizeDraft(TripAiDraft d) {
    // Keep names/titles tidy.
    String destination = titleCase(d.destination());
    List<WorthConsideringDto> worth = d.worthConsidering();
    if (worth == null || worth.isEmpty()) {
      worth =
          List.of(
              new WorthConsideringDto("A local market stroll", "A good add-on if you want more texture beyond restaurants"),
              new WorthConsideringDto("A quieter neighborhood", "A softer contrast to the main sights"));
    } else if (worth.size() > 4) {
      worth = List.of(worth.get(0), worth.get(1), worth.get(2), worth.get(3));
    }

    return new TripAiDraft(
        destination,
        d.extractedPreferences(),
        d.itinerary(),
        d.routeSummary().trim(),
        d.styleSummary().trim(),
        worth,
        d.fitSummary().trim(),
        d.tripCompanionPet());
  }

  private static String safeStr(String s, String fallback) {
    if (s == null) return fallback;
    String t = s.trim();
    return t.isEmpty() ? fallback : t;
  }

  private static <T> List<T> safeList(List<T> xs) {
    return xs == null ? List.of() : xs;
  }

  private static List<OptionalHighlightDto> toHighlights(List<TripAiJson.NamedReason> xs) {
    if (xs == null || xs.isEmpty()) return List.of();
    List<OptionalHighlightDto> out = new ArrayList<>();
    for (TripAiJson.NamedReason x : xs) {
      String n = safeStr(x.name, null);
      String r = safeStr(x.reason, null);
      if (n != null && r != null) out.add(new OptionalHighlightDto(n, r));
    }
    return out;
  }

  private static String titleCase(String s) {
    String t = s == null ? "" : s.trim();
    if (t.isEmpty()) return t;
    // avoid mangling multi-word destinations; just capitalize first letter.
    return t.substring(0, 1).toUpperCase(Locale.ROOT) + t.substring(1);
  }

  // Minimal JSON mapping types for parsing.
  static class TripAiJson {
    public String destination;
    public String routeSummary;
    public String styleSummary;
    public Extracted extractedPreferences;
    public List<Day> itinerary;
    public List<NamedReason> worthConsidering;
    public String fitSummary;
    public Pet tripCompanionPet;

    static class Extracted {
      public List<String> mustPassStops;
      public List<String> mustEat;
      public List<String> mustSeeOrDo;
      public String lodgingPreference;
      public String budgetPreference;
      public String pacePreference;
    }

    static class Day {
      public int day;
      public String title;
      public String area;
      public List<String> activities;
      public String vibeNote;
      public List<String> mustHaveCoverage;
      public List<NamedReason> optionalHighlights;
    }

    static class NamedReason {
      public String name;
      public String reason;
    }

    static class Pet {
      public String name;
      public String description;
    }
  }
}

