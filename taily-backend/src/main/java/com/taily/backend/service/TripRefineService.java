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
import com.taily.backend.dto.trip.TripGenerateWithFitResponseDto;
import com.taily.backend.dto.trip.TripProfileDto;
import com.taily.backend.dto.trip.TripRefineRequestDto;
import com.taily.backend.dto.trip.TripSummaryDto;
import com.taily.backend.dto.trip.UserProfileInputDto;
import com.taily.backend.dto.trip.WorthConsideringDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TripRefineService {
  private final IdentitySpecService identitySpecService;

  public TripRefineService(IdentitySpecService identitySpecService) {
    this.identitySpecService = identitySpecService;
  }

  public TripGenerateWithFitResponseDto refine(TripRefineRequestDto request) {
    TripSummaryDto baseSummary = request.currentTrip().tripSummary();
    List<ItineraryDayDto> baseItinerary = request.currentTrip().itinerary();
    DimensionScoresDto baseTripScores = request.currentTrip().tripProfile().dimensionScores();

    DimensionScoresDto targetTripScores = applyAdjustments(baseTripScores, request.refinement().dimensionAdjustments());

    // Preserve structure, apply small/understandable changes.
    List<ItineraryDayDto> refinedItinerary =
        adjustItineraryLightly(baseItinerary, baseSummary.destination(), baseTripScores, targetTripScores, request.refinement().prompt());

    ExtractedPreferencesDto extractedPreferences =
        inferExtractedPreferences(baseSummary, refinedItinerary, targetTripScores);

    TripProfileDto tripProfile = new TripProfileDto(targetTripScores);

    FitAnalysisDto fitAnalysis = analyzeFit(request.userProfile().dimensionScores(), targetTripScores);
    // Include the user prompt lightly to show responsiveness, but keep it calm.
    String refinedSummary = buildRefinementSummary(fitAnalysis, request.refinement().prompt());
    fitAnalysis =
        new FitAnalysisDto(
            fitAnalysis.overallFit(), fitAnalysis.topMatches(), fitAnalysis.topMismatches(), refinedSummary);

    TripSummaryDto refinedTripSummary =
        new TripSummaryDto(
            baseSummary.origin(),
            baseSummary.destination(),
            baseSummary.durationText(),
            baseSummary.budgetText(),
            buildRouteSummary(baseSummary, extractedPreferences),
            buildStyleSummary(targetTripScores, extractedPreferences));

    TravelerArchetypeDto travelerArch =
        identitySpecService.pickTravelerArchetype(request.userProfile().dimensionScores());
    ArchetypeDto travelerArchetype = new ArchetypeDto(travelerArch.name(), travelerArch.description());
    ArchetypeDto pet = identitySpecService.pickTripCompanionPet(targetTripScores);
    List<WorthConsideringDto> worthConsidering = buildWorthConsidering(baseSummary.destination(), extractedPreferences);

    String travelerSummaryLine = travelerSummaryForSpec(request.userProfile());
    TravelerIdentitySpecDto travelerIdentitySpec =
        identitySpecService.buildTravelerIdentitySpec(
            request.userProfile().dimensionScores(), travelerSummaryLine, travelerArch);
    TripCompanionSpecDto tripCompanionSpec =
        identitySpecService.buildTripCompanionSpec(targetTripScores, refinedTripSummary.styleSummary(), pet);

    return new TripGenerateWithFitResponseDto(
        refinedTripSummary,
        extractedPreferences,
        refinedItinerary,
        worthConsidering,
        tripProfile,
        fitAnalysis,
        travelerArchetype,
        pet,
        travelerIdentitySpec,
        tripCompanionSpec);
  }

  private String travelerSummaryForSpec(UserProfileInputDto userProfile) {
    String ps = userProfile.profileSummary();
    if (ps != null && !ps.isBlank()) return ps;
    return identitySpecService.travelerSummaryFromScores(userProfile.dimensionScores());
  }

  private static DimensionScoresDto applyAdjustments(DimensionScoresDto base, Map<String, Integer> adjustments) {
    int budget = base.budget();
    int pace = base.pace();
    int foodFocus = base.foodFocus();
    int sightseeing = base.sightseeing();
    int comfort = base.comfort();
    int exploration = base.exploration();

    if (adjustments != null) {
      for (Map.Entry<String, Integer> e : adjustments.entrySet()) {
        String k = norm(e.getKey());
        Integer v = e.getValue();
        if (v == null) continue;
        int score = clamp(v, 1, 5);
        switch (k) {
          case "budget" -> budget = score;
          case "pace" -> pace = score;
          case "foodfocus" -> foodFocus = score;
          case "sightseeing" -> sightseeing = score;
          case "comfort" -> comfort = score;
          case "exploration" -> exploration = score;
          default -> {
            // ignore unknown keys for MVP safety
          }
        }
      }
    }

    return new DimensionScoresDto(budget, pace, foodFocus, sightseeing, comfort, exploration);
  }

  private static List<ItineraryDayDto> adjustItineraryLightly(
      List<ItineraryDayDto> base,
      String destination,
      DimensionScoresDto baseScores,
      DimensionScoresDto targetScores,
      String prompt) {
    if (base == null || base.isEmpty()) return List.of();

    boolean slower = targetScores.pace() < baseScores.pace();
    boolean faster = targetScores.pace() > baseScores.pace();
    boolean moreComfort = targetScores.comfort() > baseScores.comfort();

    List<ItineraryDayDto> out = new ArrayList<>();
    for (ItineraryDayDto d : base) {
      List<String> activities = new ArrayList<>(d.activities());

      if (slower && activities.size() > 3) {
        // Remove one item to create breathing room.
        activities = activities.subList(0, activities.size() - 1);
      }
      if (faster && activities.size() < 5) {
        // Add one optional item to increase density.
        activities.add("Optional extra stop if you have the energy");
      }
      if (moreComfort && activities.stream().noneMatch(x -> norm(x).contains("break"))) {
        // Add a comfort pause, but only once per day.
        activities.add(Math.min(2, activities.size()), "Comfort break (cafe / rest)");
      }

      String vibeNote = d.vibeNote();
      if (slower) vibeNote = softenVibe(vibeNote);
      if (faster) vibeNote = tightenVibe(vibeNote);
      if (moreComfort) vibeNote = addComfortToVibe(vibeNote);

      out.add(
          new ItineraryDayDto(
              d.day(),
              d.title(),
              d.area() == null || d.area().isBlank() ? destination : d.area(),
              List.copyOf(activities),
              vibeNote,
              d.mustHaveCoverage(),
              d.optionalHighlights()));
    }

    // If the user prompt strongly emphasizes keeping something, do nothing destructive.
    // (MVP: we avoid heavy prompt parsing; we just avoid removing coverage items.)
    return List.copyOf(out);
  }

  private static String softenVibe(String s) {
    String t = s == null ? "" : s.trim();
    if (t.isEmpty()) return "More breathing room and a slower overall flow";
    if (t.toLowerCase(Locale.ROOT).contains("relaxed")) return t;
    return t + " (with a slower overall flow)";
  }

  private static String tightenVibe(String s) {
    String t = s == null ? "" : s.trim();
    if (t.isEmpty()) return "A slightly denser day with a bit more coverage";
    if (t.toLowerCase(Locale.ROOT).contains("high-energy")) return t;
    return t + " (slightly denser pacing)";
  }

  private static String addComfortToVibe(String s) {
    String t = s == null ? "" : s.trim();
    if (t.isEmpty()) return "Comfort-forward pacing with built-in rests";
    if (t.toLowerCase(Locale.ROOT).contains("comfort")) return t;
    return t + " (comfort-forward)";
  }

  private static ExtractedPreferencesDto inferExtractedPreferences(
      TripSummaryDto tripSummary, List<ItineraryDayDto> itinerary, DimensionScoresDto tripScores) {
    List<String> mustPassStops = new ArrayList<>();
    List<String> mustEat = new ArrayList<>();
    List<String> mustSeeOrDo = new ArrayList<>();

    for (ItineraryDayDto d : itinerary) {
      for (String c : d.mustHaveCoverage()) {
        if (c != null && !c.isBlank() && mustSeeOrDo.stream().noneMatch(x -> x.equalsIgnoreCase(c))) {
          mustSeeOrDo.add(c);
        }
      }
      if (norm(d.area()).contains("disney") && mustPassStops.stream().noneMatch(x -> norm(x).contains("disney"))) {
        mustPassStops.add(d.area());
      }
      for (String a : d.activities()) {
        String n = norm(a);
        if (n.contains("sushi") && mustEat.stream().noneMatch(x -> norm(x).contains("sushi"))) mustEat.add("Sushi");
        if (n.contains("izakaya") && mustEat.stream().noneMatch(x -> norm(x).contains("izakaya")))
          mustEat.add("Izakaya dinner");
        if ((n.contains("market") || n.contains("food")) && mustEat.isEmpty()) mustEat.add("A local signature meal");
      }
    }

    String lodgingPreference = tripScores.comfort() >= 4 ? "Higher-comfort hotel" : "Comfortable hotel";
    String budgetPreference =
        tripScores.budget() <= 2 ? "Budget-conscious" : tripScores.budget() >= 4 ? "Higher-spend" : "Balanced / mid-range";
    String pacePreference =
        tripScores.pace() <= 2 ? "Relaxed" : tripScores.pace() >= 4 ? "Packed" : "Balanced";

    return new ExtractedPreferencesDto(
        List.copyOf(mustPassStops),
        List.copyOf(mustEat),
        List.copyOf(mustSeeOrDo),
        lodgingPreference,
        budgetPreference,
        pacePreference);
  }

  private static String buildRouteSummary(TripSummaryDto base, ExtractedPreferencesDto prefs) {
    String destination = base.destination();
    String core = destination;
    if (!prefs.mustSeeOrDo().isEmpty()) {
      core += " with " + prefs.mustSeeOrDo().getFirst().toLowerCase(Locale.ROOT);
    }
    if (!prefs.mustEat().isEmpty()) {
      core += " and food-focused stops";
    }
    return core;
  }

  private static String buildStyleSummary(DimensionScoresDto tripScores, ExtractedPreferencesDto prefs) {
    String comfort = tripScores.comfort() >= 4 ? "comfortable " : "";
    String foodie = tripScores.foodFocus() >= 4 ? "foodie " : "";
    String pace = tripScores.pace() <= 2 ? "relaxed" : tripScores.pace() >= 4 ? "faster" : "balanced";
    return "A " + comfort + foodie + "trip with a " + pace + " overall pace";
  }

  private static String buildRefinementSummary(FitAnalysisDto fit, String prompt) {
    String base = fit.summary();
    String p = prompt == null ? "" : prompt.trim();
    if (p.isEmpty()) return base;
    // Keep it subtle: acknowledge the intent without quoting a long prompt.
    return base + " Updated based on your refinement.";
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

  private static List<WorthConsideringDto> buildWorthConsidering(String destination, ExtractedPreferencesDto prefs) {
    List<WorthConsideringDto> xs = new ArrayList<>();
    String d = destination.toLowerCase(Locale.ROOT);

    if (d.contains("tokyo")) {
      xs.add(new WorthConsideringDto("Kappabashi Street", "A strong optional stop if you enjoy food culture beyond restaurants"));
      xs.add(new WorthConsideringDto("Daikanyama", "A softer, more refined neighborhood that fits comfort and pace preferences"));
    } else if (!prefs.mustEat().isEmpty()) {
      xs.add(new WorthConsideringDto("A local market stroll", "A good add-on if you want more texture beyond restaurants"));
      xs.add(new WorthConsideringDto("A signature neighborhood", "A calmer area that often fits food + comfort preferences"));
    } else {
      xs.add(new WorthConsideringDto("A scenic viewpoint", "A simple add-on that makes the trip feel more vivid"));
      xs.add(new WorthConsideringDto("A quieter neighborhood", "A softer contrast to the main sights"));
    }

    return List.copyOf(xs);
  }

  private static int clamp(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
  }

  private static String norm(String s) {
    if (s == null) return "";
    return s.trim().toLowerCase(Locale.ROOT);
  }
}

