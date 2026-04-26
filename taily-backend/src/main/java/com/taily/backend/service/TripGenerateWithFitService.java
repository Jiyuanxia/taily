package com.taily.backend.service;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.identity.TravelerIdentitySpecDto;
import com.taily.backend.dto.identity.TripCompanionSpecDto;
import com.taily.backend.dto.profile.TravelerArchetypeDto;
import com.taily.backend.dto.trip.ArchetypeDto;
import com.taily.backend.dto.trip.ExtractedPreferencesDto;
import com.taily.backend.dto.trip.FitAnalysisDto;
import com.taily.backend.dto.trip.ItineraryDayDto;
import com.taily.backend.dto.trip.OptionalHighlightDto;
import com.taily.backend.dto.trip.OverallFitDto;
import com.taily.backend.dto.trip.TripGenerateWithFitRequestDto;
import com.taily.backend.dto.trip.TripGenerateWithFitResponseDto;
import com.taily.backend.dto.trip.TripProfileDto;
import com.taily.backend.dto.trip.TripSummaryDto;
import com.taily.backend.dto.trip.UserProfileInputDto;
import com.taily.backend.dto.trip.WorthConsideringDto;
import com.taily.backend.integration.openai.TripAiClient;
import com.taily.backend.integration.openai.TripAiClient.TripAiDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class TripGenerateWithFitService {
  private static final String ORIGIN_FALLBACK = "New York City";
  private static final Pattern DAYS_PATTERN =
      Pattern.compile("\\b(\\d{1,2})\\s*(?:-\\s*)?(?:day|days)\\b", Pattern.CASE_INSENSITIVE);
  private static final Pattern FROM_TO_PATTERN =
      Pattern.compile("\\bfrom\\s+([^,;.]+?)\\s+to\\s+([^,;.]+?)(?:\\b|[,.])", Pattern.CASE_INSENSITIVE);
  private static final Pattern PASS_THROUGH_PATTERN =
      Pattern.compile("\\bpass\\s+through\\s+([^.;]+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern BUDGET_DOLLAR_PATTERN =
      Pattern.compile("\\$\\s*([0-9]{1,3}(?:,[0-9]{3})*)(?:\\b|\\s)", Pattern.CASE_INSENSITIVE);

  private final TripAiClient tripAiClient;
  private final IdentitySpecService identitySpecService;

  public TripGenerateWithFitService(TripAiClient tripAiClient, IdentitySpecService identitySpecService) {
    this.tripAiClient = tripAiClient;
    this.identitySpecService = identitySpecService;
  }

  public TripGenerateWithFitResponseDto generateWithFit(TripGenerateWithFitRequestDto request) {
    String prompt = request.tripRequest().prompt();
    PromptFacts facts = parsePromptFacts(prompt);

    String origin = originValueOrFallback(request);
    if ((request.context() == null || request.context().origin() == null || request.context().origin().value() == null
            || request.context().origin().value().isBlank())
        && facts.origin != null) {
      origin = facts.origin;
    }

    Optional<TripAiDraft> aiDraft = tripAiClient.generateTripDraft(request);

    final String destination;
    final ExtractedPreferencesDto extractedPreferences;
    final List<ItineraryDayDto> itinerary;
    final List<WorthConsideringDto> worthConsidering;
    final String aiRouteSummary;
    final String aiStyleSummary;
    final String aiFitSummary;

    if (aiDraft.isPresent()) {
      TripAiDraft d = aiDraft.get();
      destination = pickDestination(d.destination(), facts);
      extractedPreferences = mergeExtractedPreferences(d.extractedPreferences(), facts);
      itinerary = normalizeItineraryDays(d.itinerary(), destination, extractedPreferences, facts.days);
      worthConsidering = d.worthConsidering();
      aiRouteSummary = patchRouteSummary(d.routeSummary(), destination, extractedPreferences, facts);
      aiStyleSummary = d.styleSummary();
      aiFitSummary = d.fitSummary();
    } else {
      destination = pickDestination(guessDestination(prompt), facts);
      extractedPreferences = mergeExtractedPreferences(extractPreferencesDeterministic(prompt), facts);
      itinerary = generateItineraryDeterministic(destination, extractedPreferences, facts.days);
      worthConsidering = buildWorthConsidering(destination, extractedPreferences);
      aiRouteSummary = null;
      aiStyleSummary = null;
      aiFitSummary = null;
    }

    DimensionScoresDto tripScores = profileTripDeterministic(extractedPreferences, itinerary);

    TripProfileDto tripProfile = new TripProfileDto(tripScores);

    FitAnalysisDto fitAnalysis = analyzeFit(request.userProfile().dimensionScores(), tripScores);
    if (aiFitSummary != null && !aiFitSummary.isBlank()) {
      fitAnalysis =
          new FitAnalysisDto(
              fitAnalysis.overallFit(), fitAnalysis.topMatches(), fitAnalysis.topMismatches(), aiFitSummary);
    }

    TripSummaryDto tripSummary =
        buildTripSummary(
            origin,
            destination,
            extractedPreferences,
            itinerary,
            request.userProfile().dimensionScores(),
            aiRouteSummary,
            aiStyleSummary);

    TravelerArchetypeDto travelerArch =
        identitySpecService.pickTravelerArchetype(request.userProfile().dimensionScores());
    ArchetypeDto travelerArchetype = new ArchetypeDto(travelerArch.name(), travelerArch.description());
    ArchetypeDto pet = identitySpecService.pickTripCompanionPet(tripScores);

    String travelerSummaryLine = travelerSummaryForSpec(request.userProfile());
    TravelerIdentitySpecDto travelerIdentitySpec =
        identitySpecService.buildTravelerIdentitySpec(
            request.userProfile().dimensionScores(), travelerSummaryLine, travelerArch);
    TripCompanionSpecDto tripCompanionSpec =
        identitySpecService.buildTripCompanionSpec(tripScores, tripSummary.styleSummary(), pet);

    return ensurePopulated(
        new TripGenerateWithFitResponseDto(
            tripSummary,
            extractedPreferences,
            itinerary,
            worthConsidering,
            tripProfile,
            fitAnalysis,
            travelerArchetype,
            pet,
            travelerIdentitySpec,
            tripCompanionSpec));
  }

  private String travelerSummaryForSpec(UserProfileInputDto userProfile) {
    String ps = userProfile.profileSummary();
    if (ps != null && !ps.isBlank()) return ps;
    return identitySpecService.travelerSummaryFromScores(userProfile.dimensionScores());
  }

  private static TripGenerateWithFitResponseDto ensurePopulated(TripGenerateWithFitResponseDto r) {
    // Contract requires stable, frontend-friendly non-null fields. Records already enforce non-null
    // at construction time (for our deterministic path). This method is future-proofing for AI.
    return r;
  }

  private static String originValueOrFallback(TripGenerateWithFitRequestDto request) {
    if (request.context() != null && request.context().origin() != null) {
      String v = request.context().origin().value();
      if (v != null && !v.isBlank()) return v;
    }
    return ORIGIN_FALLBACK;
  }

  private static String guessDestination(String prompt) {
    String p = norm(prompt);
    if (p.contains("tokyo")) return "Tokyo";
    if (p.contains("kyoto")) return "Kyoto";
    if (p.contains("paris")) return "Paris";
    if (p.contains("rome")) return "Rome";
    if (p.contains("london")) return "London";
    if (p.contains("barcelona")) return "Barcelona";
    if (p.contains("new york")) return "New York City";
    if (p.contains("nyc")) return "New York City";
    if (p.contains("los angeles") || p.contains("la ")) return "Los Angeles";
    if (p.contains("san francisco") || p.contains("sf")) return "San Francisco";
    if (p.contains("mexico city")) return "Mexico City";
    if (p.contains("seoul")) return "Seoul";
    return "Your destination";
  }

  private static ExtractedPreferencesDto extractPreferencesDeterministic(String prompt) {
    String p = norm(prompt);

    List<String> mustPassStops = new ArrayList<>();
    List<String> mustEat = new ArrayList<>();
    List<String> mustSeeOrDo = new ArrayList<>();

    if (p.contains("disney")) {
      mustPassStops.add("Tokyo DisneySea");
      mustSeeOrDo.add("One Disney day");
    }

    if (p.contains("sushi")) mustEat.add("Sushi");
    if (p.contains("izakaya")) mustEat.add("Izakaya dinner");
    if (mustEat.isEmpty() && (p.contains("food") || p.contains("foodie"))) {
      mustEat.add("A local signature meal");
    }

    String lodgingPreference =
        (p.contains("hotel") && (p.contains("mid") || p.contains("mid-range") || p.contains("midrange")))
            ? "Mid-range hotel"
            : p.contains("luxury")
                ? "Higher-comfort hotel"
                : p.contains("budget")
                    ? "Budget-friendly stay"
                    : "Comfortable hotel";

    String budgetPreference =
        (p.contains("mid") || p.contains("mid-range") || p.contains("midrange"))
            ? "Balanced / mid-range"
            : p.contains("budget") ? "Budget-conscious" : p.contains("luxury") ? "Higher-spend" : "Balanced";

    String pacePreference =
        p.contains("relaxed") || p.contains("slow") ? "Relaxed" : p.contains("packed") ? "Packed" : "Balanced";

    return new ExtractedPreferencesDto(
        mustPassStops, mustEat, mustSeeOrDo, lodgingPreference, budgetPreference, pacePreference);
  }

  private static List<ItineraryDayDto> generateItineraryDeterministic(
      String destination, ExtractedPreferencesDto prefs, Integer explicitDays) {
    int days = explicitDays != null ? clamp(explicitDays, 1, 14) : 5; // stable default for first pass

    List<ItineraryDayDto> itinerary = new ArrayList<>();
    for (int d = 1; d <= days; d++) {
      itinerary.add(buildDay(d, destination, prefs));
    }

    // If Disney is requested, make day 2 the "Disney day" per contract example for stability.
    if (prefs.mustPassStops().stream().anyMatch(x -> norm(x).contains("disney"))) {
      itinerary.set(
          1,
          new ItineraryDayDto(
              2,
              "Disney day",
              prefs.mustPassStops().getFirst(),
              List.of("Full-day Disney visit", "Evening return with casual dinner"),
              "High-energy day compared with the rest of the trip",
              List.of("One Disney day"),
              List.of(new OptionalHighlightDto("Ikspiari", "A good nearby dining option for a slower end to the day"))));
    }

    return itinerary;
  }

  private static ItineraryDayDto buildDay(int day, String destination, ExtractedPreferencesDto prefs) {
    if (day == 1) {
      return new ItineraryDayDto(
          1,
          "Easy arrival and evening reset",
          destination,
          List.of("Check-in", "Light neighborhood walk", "Unhurried dinner"),
          "A relaxed start with light movement",
          prefs.mustEat().isEmpty() ? List.of() : List.of(prefs.mustEat().getFirst()),
          List.of(
              new OptionalHighlightDto(
                  "Scenic viewpoint",
                  "A low-effort add-on if you want a stronger first-night sense of place")));
    }

    if (day == 3) {
      return new ItineraryDayDto(
          3,
          "Food-first wandering and local favorites",
          destination,
          List.of("Market or food street", "Signature lunch", "Cozy cafe break", "Dinner with a local specialty"),
          "A day that leans into taste and texture",
          prefs.mustEat().isEmpty() ? List.of() : List.of(prefs.mustEat().getFirst()),
          List.of(new OptionalHighlightDto("Small neighborhood detour", "A softer area to slow down and explore")));
    }

    if (day == 4) {
      return new ItineraryDayDto(
          4,
          "Classic sights with breathing room",
          destination,
          List.of("One major landmark", "Unstructured stroll", "Easy dinner"),
          "Balanced movement with downtime baked in",
          prefs.mustSeeOrDo().isEmpty() ? List.of() : List.of(prefs.mustSeeOrDo().getFirst()),
          List.of(new OptionalHighlightDto("Museum or garden", "A calm option if you want more depth")));
    }

    return new ItineraryDayDto(
        day,
        "Flexible day for favorites and surprises",
        destination,
        List.of("Choose a favorite area", "Optional short excursion", "Relaxed evening"),
        "A lighter day to adjust based on energy",
        List.of(),
        List.of(new OptionalHighlightDto("Neighborhood recommendation", "A good fit if you want something less expected")));
  }

  private static DimensionScoresDto profileTripDeterministic(
      ExtractedPreferencesDto prefs, List<ItineraryDayDto> itinerary) {
    int budget = prefs.budgetPreference().toLowerCase(Locale.ROOT).contains("budget") ? 2 : 3;
    if (prefs.budgetPreference().toLowerCase(Locale.ROOT).contains("higher")) budget = 4;

    int pace = prefs.pacePreference().equalsIgnoreCase("Relaxed") ? 3 : 3;
    if (prefs.pacePreference().equalsIgnoreCase("Packed")) pace = 4;
    if (prefs.mustSeeOrDo().stream().anyMatch(x -> norm(x).contains("disney"))) pace = Math.max(pace, 3);

    int foodFocus = prefs.mustEat().isEmpty() ? 3 : 5;
    int sightseeing = prefs.mustSeeOrDo().isEmpty() ? 3 : 4;
    int comfort = prefs.lodgingPreference().toLowerCase(Locale.ROOT).contains("budget") ? 2 : 4;
    int exploration = 3;

    return new DimensionScoresDto(budget, pace, foodFocus, sightseeing, comfort, exploration);
  }

  private static FitAnalysisDto analyzeFit(DimensionScoresDto user, DimensionScoresDto trip) {
    int diffSum =
        Math.abs(user.budget() - trip.budget())
            + Math.abs(user.pace() - trip.pace())
            + Math.abs(user.foodFocus() - trip.foodFocus())
            + Math.abs(user.sightseeing() - trip.sightseeing())
            + Math.abs(user.comfort() - trip.comfort())
            + Math.abs(user.exploration() - trip.exploration());

    int score = clamp(100 - diffSum * 8, 0, 100);
    String label = score >= 80 ? "Strong Fit" : score >= 60 ? "Moderate Fit" : "Weak Fit";

    List<String> topMatches = new ArrayList<>();
    List<String> topMismatches = new ArrayList<>();

    addMatchOrMismatch("Budget", user.budget(), trip.budget(), topMatches, topMismatches);
    addMatchOrMismatch("Pace", user.pace(), trip.pace(), topMatches, topMismatches);
    addMatchOrMismatch("Food Focus", user.foodFocus(), trip.foodFocus(), topMatches, topMismatches);
    addMatchOrMismatch("Sightseeing", user.sightseeing(), trip.sightseeing(), topMatches, topMismatches);
    addMatchOrMismatch("Comfort", user.comfort(), trip.comfort(), topMatches, topMismatches);
    addMatchOrMismatch("Exploration", user.exploration(), trip.exploration(), topMatches, topMismatches);

    // Keep concise: pick up to 2 matches/mismatches, but never return empty arrays.
    topMatches = trimToAtLeastOne(topMatches, "Comfort");
    topMismatches = trimToAtLeastOne(topMismatches, "Pace");

    String summary =
        "This trip aligns best on "
            + topMatches.getFirst().toLowerCase(Locale.ROOT)
            + ", with the biggest tension around "
            + topMismatches.getFirst().toLowerCase(Locale.ROOT)
            + ".";

    return new FitAnalysisDto(new OverallFitDto(score, label), topMatches, topMismatches, summary);
  }

  private static List<String> trimToAtLeastOne(List<String> xs, String fallback) {
    if (xs.isEmpty()) return List.of(fallback);
    return xs.size() <= 2 ? List.copyOf(xs) : List.of(xs.get(0), xs.get(1));
  }

  private static void addMatchOrMismatch(
      String label,
      int user,
      int trip,
      List<String> matches,
      List<String> mismatches) {
    int d = Math.abs(user - trip);
    if (d <= 1) matches.add(label);
    if (d >= 3) mismatches.add(label);
  }

  private static TripSummaryDto buildTripSummary(
      String origin,
      String destination,
      ExtractedPreferencesDto prefs,
      List<ItineraryDayDto> itinerary,
      DimensionScoresDto userScores,
      String aiRouteSummary,
      String aiStyleSummary) {
    String durationText = itinerary.size() + " days";

    String budgetText =
        prefs.budgetPreference().toLowerCase(Locale.ROOT).contains("budget")
            ? "Budget"
            : prefs.budgetPreference().toLowerCase(Locale.ROOT).contains("higher") ? "Higher-spend" : "Mid-range";

    String routeSummary = (aiRouteSummary != null && !aiRouteSummary.isBlank()) ? aiRouteSummary : null;
    if (routeSummary == null) {
      routeSummary =
          destination
              + (prefs.mustSeeOrDo().isEmpty()
                  ? ""
                  : " with " + prefs.mustSeeOrDo().getFirst().toLowerCase(Locale.ROOT))
              + (prefs.mustEat().isEmpty() ? "" : " and food-focused stops");
    }

    String styleSummary = (aiStyleSummary != null && !aiStyleSummary.isBlank()) ? aiStyleSummary : null;
    if (styleSummary == null) {
      styleSummary =
          (userScores.comfort() >= 4 ? "A comfortable " : "A ")
              + (userScores.foodFocus() >= 4 ? "foodie " : "")
              + "trip with a "
              + (userScores.pace() <= 2 ? "relaxed" : userScores.pace() >= 4 ? "faster" : "balanced")
              + " overall pace";
    }

    return new TripSummaryDto(origin, destination, durationText, budgetText, routeSummary, styleSummary);
  }

  private static List<WorthConsideringDto> buildWorthConsidering(String destination, ExtractedPreferencesDto prefs) {
    List<WorthConsideringDto> xs = new ArrayList<>();
    String d = destination.toLowerCase(Locale.ROOT);

    if (d.contains("tokyo")) {
      xs.add(new WorthConsideringDto("Kappabashi Street", "A strong optional stop if you enjoy food culture beyond restaurants"));
      xs.add(new WorthConsideringDto("Daikanyama", "A softer, more refined neighborhood that fits comfort and pace preferences"));
    } else if (prefs.mustEat().size() > 0) {
      xs.add(new WorthConsideringDto("A local market stroll", "A good add-on if you want more texture beyond restaurants"));
      xs.add(new WorthConsideringDto("A signature neighborhood", "A calmer area that often fits food + comfort preferences"));
    } else {
      xs.add(new WorthConsideringDto("A scenic viewpoint", "A simple add-on that makes the trip feel more vivid"));
      xs.add(new WorthConsideringDto("A quieter neighborhood", "A softer contrast to the main sights"));
    }

    return xs;
  }

  private static int clamp(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
  }

  private static String norm(String s) {
    if (s == null) return "";
    return s.trim().toLowerCase(Locale.ROOT);
  }

  private static String pickDestination(String base, PromptFacts facts) {
    if (facts.destination != null) return facts.destination;
    return base;
  }

  private static ExtractedPreferencesDto mergeExtractedPreferences(ExtractedPreferencesDto base, PromptFacts facts) {
    List<String> mustPassStops = new ArrayList<>(safeList(base.mustPassStops()));
    for (String s : facts.mustPassStops) addIfMissing(mustPassStops, s);

    List<String> mustEat = new ArrayList<>(safeList(base.mustEat()));
    for (String s : facts.mustEat) addIfMissing(mustEat, s);

    List<String> mustSeeOrDo = new ArrayList<>(safeList(base.mustSeeOrDo()));
    for (String s : facts.mustSeeOrDo) addIfMissing(mustSeeOrDo, s);

    String lodgingPreference = facts.lodgingPreference != null ? facts.lodgingPreference : base.lodgingPreference();
    String budgetPreference = facts.budgetPreference != null ? facts.budgetPreference : base.budgetPreference();
    String pacePreference = facts.pacePreference != null ? facts.pacePreference : base.pacePreference();

    return new ExtractedPreferencesDto(
        List.copyOf(mustPassStops),
        List.copyOf(mustEat),
        List.copyOf(mustSeeOrDo),
        lodgingPreference,
        budgetPreference,
        pacePreference);
  }

  private static List<ItineraryDayDto> normalizeItineraryDays(
      List<ItineraryDayDto> base,
      String destination,
      ExtractedPreferencesDto prefs,
      Integer explicitDays) {
    if (base == null || base.isEmpty()) return generateItineraryDeterministic(destination, prefs, explicitDays);
    int desired = explicitDays != null ? clamp(explicitDays, 1, 14) : base.size();
    List<ItineraryDayDto> out = new ArrayList<>();

    // Trim or keep first N.
    int n = Math.min(desired, base.size());
    for (int i = 0; i < n; i++) {
      ItineraryDayDto d = base.get(i);
      out.add(
          new ItineraryDayDto(
              i + 1,
              d.title(),
              (d.area() == null || d.area().isBlank()) ? destination : d.area(),
              d.activities(),
              d.vibeNote(),
              d.mustHaveCoverage(),
              d.optionalHighlights()));
    }
    // Pad if needed.
    for (int day = out.size() + 1; day <= desired; day++) {
      out.add(buildDay(day, destination, prefs));
    }

    // Honor explicit pass-through stops by ensuring they're visible in the day areas (in order).
    int idx = 0;
    for (String stop : prefs.mustPassStops()) {
      if (idx >= out.size()) break;
      if (!containsIgnoreCase(out.get(idx).area(), stop)) {
        ItineraryDayDto d = out.get(idx);
        out.set(
            idx,
            new ItineraryDayDto(
                d.day(),
                d.title(),
                stop,
                prependOnce(d.activities(), "Pass through " + stop),
                d.vibeNote(),
                d.mustHaveCoverage(),
                d.optionalHighlights()));
      }
      idx++;
    }

    return List.copyOf(out);
  }

  private static String patchRouteSummary(
      String aiRouteSummary, String destination, ExtractedPreferencesDto prefs, PromptFacts facts) {
    // If AI summary already includes the explicit stops, keep it.
    if (aiRouteSummary != null && !aiRouteSummary.isBlank()) {
      boolean ok = true;
      for (String s : facts.mustPassStops) {
        if (!containsIgnoreCase(aiRouteSummary, s)) {
          ok = false;
          break;
        }
      }
      if (ok) return aiRouteSummary;
    }

    // Deterministic summary: origin -> stops -> destination (+ musts).
    StringBuilder sb = new StringBuilder();
    if (facts.origin != null && !facts.origin.isBlank()) sb.append(facts.origin).append(" to ");
    sb.append(destination);
    if (!prefs.mustPassStops().isEmpty()) {
      sb.append(" via ").append(String.join(" → ", prefs.mustPassStops()));
    }
    if (!prefs.mustSeeOrDo().isEmpty()) {
      sb.append(" (").append(prefs.mustSeeOrDo().getFirst()).append(")");
    }
    if (!prefs.mustEat().isEmpty()) {
      sb.append(" with ").append(prefs.mustEat().getFirst().toLowerCase(Locale.ROOT));
    }
    return sb.toString();
  }

  private static boolean containsIgnoreCase(String haystack, String needle) {
    if (haystack == null || needle == null) return false;
    return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
  }

  private static List<String> prependOnce(List<String> activities, String item) {
    List<String> xs = new ArrayList<>(safeList(activities));
    for (String a : xs) {
      if (containsIgnoreCase(a, item)) return xs;
    }
    xs.addFirst(item);
    return List.copyOf(xs);
  }

  private static void addIfMissing(List<String> xs, String value) {
    if (value == null || value.isBlank()) return;
    for (String x : xs) {
      if (x != null && x.equalsIgnoreCase(value)) return;
    }
    xs.add(value);
  }

  private static <T> List<T> safeList(List<T> xs) {
    return xs == null ? List.of() : xs;
  }

  private static PromptFacts parsePromptFacts(String prompt) {
    String p = prompt == null ? "" : prompt.trim();
    String pn = norm(p);

    Integer days = null;
    Matcher dm = DAYS_PATTERN.matcher(pn);
    if (dm.find()) {
      try {
        days = Integer.parseInt(dm.group(1));
      } catch (Exception ignored) {
        // ignore
      }
    }

    String origin = null;
    String destination = null;
    Matcher ft = FROM_TO_PATTERN.matcher(p);
    if (ft.find()) {
      origin = tidyPlace(ft.group(1));
      destination = tidyPlace(ft.group(2));
    }

    List<String> mustPassStops = new ArrayList<>();
    Matcher pm = PASS_THROUGH_PATTERN.matcher(p);
    if (pm.find()) {
      String stops = pm.group(1);
      for (String part : stops.split(",")) {
        String s = tidyPlace(part);
        if (s != null) addIfMissing(mustPassStops, s);
      }
      // handle "pass through X and Y"
      if (mustPassStops.size() <= 1 && stops.toLowerCase(Locale.ROOT).contains(" and ")) {
        for (String part : stops.split("\\band\\b")) {
          String s = tidyPlace(part);
          if (s != null) addIfMissing(mustPassStops, s);
        }
      }
    }

    String lodgingPreference = null;
    if (pn.contains("mid-range") || pn.contains("midrange")) lodgingPreference = "Mid-range hotels";
    else if (pn.contains("luxury")) lodgingPreference = "Higher-comfort hotel";
    else if (pn.contains("budget hotel") || pn.contains("cheap hotel")) lodgingPreference = "Budget-friendly stay";

    String pacePreference = null;
    if (pn.contains("relaxed") || pn.contains("slow pace")) pacePreference = "Relaxed";
    else if (pn.contains("packed") || pn.contains("fast-paced") || pn.contains("fast pace")) pacePreference = "Packed";

    String budgetPreference = null;
    Matcher bm = BUDGET_DOLLAR_PATTERN.matcher(p);
    if (bm.find()) {
      budgetPreference = "Around $" + bm.group(1).replace(",", "");
    } else if (pn.contains("budget")) {
      budgetPreference = "Budget-conscious";
    } else if (pn.contains("mid-range") || pn.contains("midrange")) {
      budgetPreference = "Balanced / mid-range";
    } else if (pn.contains("luxury")) {
      budgetPreference = "Higher-spend";
    }

    List<String> mustEat = new ArrayList<>();
    if (pn.contains("must eat")) {
      // very small heuristic: pick the food word after "must eat"
      int idx = pn.indexOf("must eat");
      String tail = idx >= 0 ? p.substring(Math.min(p.length(), idx + "must eat".length())) : "";
      String candidate = tail.replaceAll("[.]", " ").trim();
      if (!candidate.isBlank()) {
        String first = candidate.split("[,;]")[0].trim();
        if (!first.isBlank() && first.length() <= 48) addIfMissing(mustEat, titleish(first));
      }
    }
    if (pn.contains("seafood")) addIfMissing(mustEat, "Seafood");

    List<String> mustSeeOrDo = new ArrayList<>();
    if (pn.contains("must see")) addIfMissing(mustSeeOrDo, "Must-see items from your prompt");
    if (pn.contains("must do")) addIfMissing(mustSeeOrDo, "Must-do items from your prompt");

    return new PromptFacts(days, origin, destination, mustPassStops, mustEat, mustSeeOrDo, lodgingPreference, budgetPreference, pacePreference);
  }

  private static String tidyPlace(String raw) {
    if (raw == null) return null;
    String t = raw.trim();
    t = t.replaceAll("\\b(?:a|an|the)\\b", "").trim();
    t = t.replaceAll("\\s+", " ").trim();
    if (t.isEmpty()) return null;
    // avoid overly long captures
    if (t.length() > 60) t = t.substring(0, 60).trim();
    return titleish(t);
  }

  private static String titleish(String s) {
    String t = s == null ? "" : s.trim();
    if (t.isEmpty()) return t;
    return t.substring(0, 1).toUpperCase(Locale.ROOT) + t.substring(1);
  }

  private record PromptFacts(
      Integer days,
      String origin,
      String destination,
      List<String> mustPassStops,
      List<String> mustEat,
      List<String> mustSeeOrDo,
      String lodgingPreference,
      String budgetPreference,
      String pacePreference) {}
}

