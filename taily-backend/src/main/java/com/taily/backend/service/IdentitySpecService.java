package com.taily.backend.service;

import com.taily.backend.dto.common.DimensionScoresDto;
import com.taily.backend.dto.identity.DimensionLevelsDto;
import com.taily.backend.dto.identity.IdentityBackgroundDto;
import com.taily.backend.dto.identity.IdentityVisualSlotDto;
import com.taily.backend.dto.identity.TravelerIdentitySpecDto;
import com.taily.backend.dto.identity.TripCompanionSpecDto;
import com.taily.backend.dto.profile.TravelerArchetypeDto;
import com.taily.backend.dto.trip.ArchetypeDto;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Builds v1 identity card specs: dimension levels, visual slots, backgrounds, tags, and
 * image-generation prompts. Archetype selection is deterministic from dimension scores.
 */
@Service
public class IdentitySpecService {

  public static final String TRAVELER_KIND = "traveler";
  public static final String COMPANION_KIND = "trip_companion_pet";
  private static final String SPEC_HASH_PREFIX = "TailyIdentityV1";

  /** One-line traveler summary from scores (used when full profile summary is not on the request). */
  public String travelerSummaryFromScores(DimensionScoresDto s) {
    List<String> highs = new ArrayList<>();
    if (s.foodFocus() >= 4) highs.add("food");
    if (s.comfort() >= 4) highs.add("comfort");
    if (s.exploration() >= 4) highs.add("exploration");
    if (s.sightseeing() >= 4) highs.add("sightseeing");

    String pace =
        (s.pace() <= 2) ? "slower-paced" : (s.pace() >= 4) ? "faster-paced" : "balanced-pace";
    String budget =
        (s.budget() <= 2) ? "budget-conscious" : (s.budget() >= 4) ? "higher-spend" : "mid-range";

    String focus;
    if (highs.isEmpty()) {
      focus = "a balanced mix of experiences";
    } else if (highs.size() == 1) {
      focus = "a strong focus on " + highs.getFirst();
    } else if (highs.size() == 2) {
      focus = "a strong focus on " + highs.get(0) + " and " + highs.get(1);
    } else {
      focus = "a strong focus on " + String.join(", ", highs);
    }

    return "You prefer " + pace + ", " + budget + " trips with " + focus + ".";
  }

  public TravelerArchetypeDto pickTravelerArchetype(DimensionScoresDto s) {
    if ((s.pace() <= 2 && s.foodFocus() >= 4)
        || (s.pace() <= 3 && s.foodFocus() >= 5 && s.comfort() >= 4)) {
      return new TravelerArchetypeDto(
          "Slow Food Dreamer",
          "A cozy traveler who values good food, gentle pacing, and comfortable experiences.");
    }
    if ((s.pace() >= 4 && (s.exploration() >= 4 || s.sightseeing() >= 4))
        || (s.pace() >= 5 && s.exploration() >= 3)) {
      return new TravelerArchetypeDto(
          "Fast Adventure Chaser",
          "An energetic traveler who seeks movement, discovery, and exciting experiences.");
    }
    return new TravelerArchetypeDto(
        "Balanced City Explorer",
        "A balanced traveler who enjoys discovering cities, local highlights, and a well-paced itinerary.");
  }

  public ArchetypeDto pickTripCompanionPet(DimensionScoresDto trip) {
    if (trip.exploration() >= 4 || (trip.sightseeing() >= 4 && trip.pace() >= 3)) {
      return new ArchetypeDto(
          "Curious Scout Fox",
          "A curious little guide who loves discovering places, details, and hidden gems.");
    }
    if (trip.pace() <= 2 && (trip.comfort() >= 3 || trip.foodFocus() >= 4)) {
      return new ArchetypeDto(
          "Lazy Carpet Cat",
          "A laid-back companion who turns the trip into a cozy and indulgent journey.");
    }
    if (trip.comfort() >= 4) {
      return new ArchetypeDto(
          "Cozy Travel Bear",
          "A warm travel buddy who makes the trip feel safe, soft, and comfortable.");
    }
    return new ArchetypeDto(
        "Cozy Travel Bear",
        "A warm travel buddy who makes the trip feel safe, soft, and comfortable.");
  }

  public TravelerIdentitySpecDto buildTravelerIdentitySpec(
      DimensionScoresDto scores, String profileSummary, TravelerArchetypeDto archetype) {
    DimensionLevelsDto levels = toLevels(scores);
    String id = travelerArchetypeId(archetype.name());
    List<String> moodTags = travelerMoodTags(id);
    IdentityBackgroundDto bg = travelerBackground(id);
    List<IdentityVisualSlotDto> slots = buildTravelerSlots(levels, scores);
    List<String> visualTags = buildTravelerVisualTags(scores, id, moodTags);
    String prompt =
        buildTravelerImagePrompt(
            archetype.name(), archetype.description(), moodTags, profileSummary, slots, bg);
    String specHash = sha256Hex(SPEC_HASH_PREFIX + "|" + TRAVELER_KIND + "|" + id + "|" + prompt);
    return new TravelerIdentitySpecDto(
        TRAVELER_KIND,
        id,
        archetype.name(),
        archetype.description(),
        moodTags,
        profileSummary,
        levels,
        slots,
        bg,
        visualTags,
        prompt,
        specHash);
  }

  public TripCompanionSpecDto buildTripCompanionSpec(
      DimensionScoresDto tripScores, String tripSummaryLine, ArchetypeDto pet) {
    DimensionLevelsDto levels = toLevels(tripScores);
    String id = companionArchetypeId(pet.name());
    List<String> moodTags = companionMoodTags(id);
    IdentityBackgroundDto bg = companionBackground(id);
    List<IdentityVisualSlotDto> slots = buildCompanionSlots(levels, tripScores);
    List<String> visualTags = buildCompanionVisualTags(tripScores, id, moodTags);
    String prompt =
        buildCompanionImagePrompt(
            pet.name(), pet.description(), moodTags, tripSummaryLine, slots, bg);
    String specHash = sha256Hex(SPEC_HASH_PREFIX + "|" + COMPANION_KIND + "|" + id + "|" + prompt);
    return new TripCompanionSpecDto(
        COMPANION_KIND,
        id,
        pet.name(),
        pet.description(),
        moodTags,
        tripSummaryLine,
        levels,
        slots,
        bg,
        visualTags,
        prompt,
        specHash);
  }

  static String sha256Hex(String input) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static String travelerArchetypeId(String name) {
    return switch (name) {
      case "Slow Food Dreamer" -> "slow_food_dreamer";
      case "Fast Adventure Chaser" -> "fast_adventure_chaser";
      default -> "balanced_city_explorer";
    };
  }

  private static String companionArchetypeId(String name) {
    return switch (name) {
      case "Lazy Carpet Cat" -> "lazy_carpet_cat";
      case "Curious Scout Fox" -> "curious_scout_fox";
      default -> "cozy_travel_bear";
    };
  }

  private static List<String> travelerMoodTags(String archetypeId) {
    return switch (archetypeId) {
      case "slow_food_dreamer" -> List.of("warm", "cozy", "relaxed");
      case "fast_adventure_chaser" -> List.of("energetic", "bold", "adventurous");
      default -> List.of("balanced", "curious", "urban");
    };
  }

  private static List<String> companionMoodTags(String archetypeId) {
    return switch (archetypeId) {
      case "lazy_carpet_cat" -> List.of("playful", "soft", "cozy");
      case "curious_scout_fox" -> List.of("curious", "agile", "exploratory");
      default -> List.of("warm", "gentle", "reliable");
    };
  }

  private static IdentityBackgroundDto travelerBackground(String archetypeId) {
    return switch (archetypeId) {
      case "slow_food_dreamer" ->
          new IdentityBackgroundDto(
              "cozy travel street corner",
              "soft café facade, hanging lights, small travel signage",
              "warm beige, cream, muted peach");
      case "fast_adventure_chaser" ->
          new IdentityBackgroundDto(
              "open travel route landscape",
              "path lines, distant hills, directional markers",
              "soft sky blue, dusty green, pale orange");
      default ->
          new IdentityBackgroundDto(
              "clean city street scene",
              "light building silhouettes, route lines, subtle city signage",
              "light gray, muted blue, pale sage");
    };
  }

  private static IdentityBackgroundDto companionBackground(String archetypeId) {
    return switch (archetypeId) {
      case "lazy_carpet_cat" ->
          new IdentityBackgroundDto(
              "dreamy soft travel sky",
              "gentle clouds, floating sparkles, soft horizon accents",
              "cream, pale gold, soft blue");
      case "curious_scout_fox" ->
          new IdentityBackgroundDto(
              "playful explorer backdrop",
              "map textures, path lines, small flags, subtle landmarks",
              "pale green, sand beige, light orange");
      default ->
          new IdentityBackgroundDto(
              "warm travel comfort backdrop",
              "soft seating shapes, warm interior travel hints, steam-like accents",
              "warm cream, soft tan, muted rose");
    };
  }

  private static DimensionLevelsDto toLevels(DimensionScoresDto s) {
    return new DimensionLevelsDto(
        level(s.budget()),
        level(s.pace()),
        level(s.foodFocus()),
        level(s.sightseeing()),
        level(s.comfort()),
        level(s.exploration()));
  }

  static String level(int score) {
    int v = Math.min(5, Math.max(1, score));
    if (v <= 2) return "low";
    if (v == 3) return "medium";
    return "high";
  }

  private static List<IdentityVisualSlotDto> buildTravelerSlots(
      DimensionLevelsDto levels, DimensionScoresDto scores) {
    List<IdentityVisualSlotDto> out = new ArrayList<>();
    out.add(
        new IdentityVisualSlotDto(
            "shoes",
            "pace",
            levels.pace(),
            travelerPaceShoes(level(scores.pace()))));
    out.add(
        new IdentityVisualSlotDto(
            "bag",
            "budget",
            levels.budget(),
            travelerBudgetBag(level(scores.budget()))));
    out.add(
        new IdentityVisualSlotDto(
            "hand_item",
            "foodFocus",
            levels.foodFocus(),
            travelerFoodHand(level(scores.foodFocus()))));
    out.add(
        new IdentityVisualSlotDto(
            "head_or_viewing_accessory",
            "sightseeing",
            levels.sightseeing(),
            travelerSightseeingHead(level(scores.sightseeing()))));
    out.add(
        new IdentityVisualSlotDto(
            "outerwear",
            "comfort",
            levels.comfort(),
            travelerComfortOuterwear(level(scores.comfort()))));
    out.add(
        new IdentityVisualSlotDto(
            "map_or_compass_item",
            "exploration",
            levels.exploration(),
            travelerExplorationItem(level(scores.exploration()))));
    return out;
  }

  private static String travelerPaceShoes(String lv) {
    return switch (lv) {
      case "low" -> "soft relaxed flats";
      case "medium" -> "comfortable travel sneakers";
      default -> "sporty running shoes";
    };
  }

  private static String travelerBudgetBag(String lv) {
    return switch (lv) {
      case "low" -> "simple canvas bag";
      case "medium" -> "neat travel backpack";
      default -> "refined premium bag or mini suitcase";
    };
  }

  private static String travelerFoodHand(String lv) {
    return switch (lv) {
      case "low" -> "coffee cup";
      case "medium" -> "snack or dessert";
      default -> "obvious food item or street food";
    };
  }

  private static String travelerSightseeingHead(String lv) {
    return switch (lv) {
      case "low" -> "simple cap";
      case "medium" -> "camera charm";
      default -> "camera or mini binoculars";
    };
  }

  private static String travelerComfortOuterwear(String lv) {
    return switch (lv) {
      case "low" -> "light top";
      case "medium" -> "casual jacket";
      default -> "soft scarf, shawl, or cozy layer";
    };
  }

  private static String travelerExplorationItem(String lv) {
    return switch (lv) {
      case "low" -> "travel note card";
      case "medium" -> "folded city map";
      default -> "compass or map scroll";
    };
  }

  private static List<IdentityVisualSlotDto> buildCompanionSlots(
      DimensionLevelsDto levels, DimensionScoresDto scores) {
    List<IdentityVisualSlotDto> out = new ArrayList<>();
    out.add(
        new IdentityVisualSlotDto(
            "movement_style",
            "pace",
            levels.pace(),
            companionPaceMovement(level(scores.pace()))));
    out.add(
        new IdentityVisualSlotDto(
            "gear_quality",
            "budget",
            levels.budget(),
            companionBudgetGear(level(scores.budget()))));
    out.add(
        new IdentityVisualSlotDto(
            "food_prop",
            "foodFocus",
            levels.foodFocus(),
            companionFoodProp(level(scores.foodFocus()))));
    out.add(
        new IdentityVisualSlotDto(
            "head_accessory",
            "sightseeing",
            levels.sightseeing(),
            companionSightseeingHead(level(scores.sightseeing()))));
    out.add(
        new IdentityVisualSlotDto(
            "comfort_item",
            "comfort",
            levels.comfort(),
            companionComfortItem(level(scores.comfort()))));
    out.add(
        new IdentityVisualSlotDto(
            "explorer_item",
            "exploration",
            levels.exploration(),
            companionExplorerItem(level(scores.exploration()))));
    return out;
  }

  private static String companionPaceMovement(String lv) {
    return switch (lv) {
      case "low" -> "floating slowly or lounging";
      case "medium" -> "walking lightly";
      default -> "dashing or gliding energetically";
    };
  }

  private static String companionBudgetGear(String lv) {
    return switch (lv) {
      case "low" -> "simple gear";
      case "medium" -> "tidy travel gear";
      default -> "polished fancy gear";
    };
  }

  private static String companionFoodProp(String lv) {
    return switch (lv) {
      case "low" -> "tiny snack";
      case "medium" -> "drink or treat";
      default -> "prominent food item";
    };
  }

  private static String companionSightseeingHead(String lv) {
    return switch (lv) {
      case "low" -> "simple hat";
      case "medium" -> "travel cap";
      default -> "goggles, camera, or sightseeing gear";
    };
  }

  private static String companionComfortItem(String lv) {
    return switch (lv) {
      case "low" -> "minimal seat";
      case "medium" -> "soft cushion";
      default -> "plush blanket or comfy cushion";
    };
  }

  private static String companionExplorerItem(String lv) {
    return switch (lv) {
      case "low" -> "small note card";
      case "medium" -> "folded map";
      default -> "compass, satchel, or explorer kit";
    };
  }

  private static List<String> buildTravelerVisualTags(
      DimensionScoresDto s, String archetypeId, List<String> moodTags) {
    Set<String> tags = new LinkedHashSet<>(moodTags);
    if (s.foodFocus() >= 4) tags.add("food-led");
    if (s.pace() <= 2) tags.add("gentle-pace");
    if (s.pace() >= 4) tags.add("high-pace");
    if (s.comfort() >= 4) tags.add("comfort-first");
    if (s.exploration() >= 4) tags.add("discovery-led");
    if (s.sightseeing() >= 4) tags.add("highlights-led");
    tags.add(archetypeId.replace("_", "-"));
    return new ArrayList<>(tags);
  }

  private static List<String> buildCompanionVisualTags(
      DimensionScoresDto s, String archetypeId, List<String> moodTags) {
    Set<String> tags = new LinkedHashSet<>(moodTags);
    if (s.exploration() >= 4) tags.add("explorer-trip");
    if (s.pace() <= 2) tags.add("slow-trip");
    if (s.comfort() >= 4) tags.add("soft-trip");
    tags.add(archetypeId.replace("_", "-"));
    return new ArrayList<>(tags);
  }

  private static String buildTravelerImagePrompt(
      String archetypeName,
      String archetypeDescription,
      List<String> moodTags,
      String summary,
      List<IdentityVisualSlotDto> slots,
      IdentityBackgroundDto bg) {
    String style =
        "Soft editorial illustration, clean 2D character art, gentle shading, pastel and near-white palette, "
            + "product-friendly composition, one centered friendly stylized adult traveler character, "
            + "simple supportive background, portrait-card framing, no text, no lettering, no logos, no watermarks.";
    String mood = String.join(", ", moodTags);
    StringBuilder accessories = new StringBuilder();
    for (IdentityVisualSlotDto sl : slots) {
      accessories.append(
          String.format(
              Locale.ROOT,
              " %s (%s): %s.",
              humanizeSlot(sl.slot()),
              sl.dimension(),
              sl.descriptor()));
    }
    return String.join(
            " ",
            style,
            "Archetype:",
            archetypeName + ".",
            "Mood:",
            mood + ".",
            "Character idea:",
            archetypeDescription,
            "Profile hint:",
            clampSentence(summary, 220),
            "Wardrobe and props:",
            accessories.toString().trim(),
            "Background setting:",
            bg.setting() + ".",
            "Background elements:",
            bg.elements() + ".",
            "Background color mood:",
            bg.colorMood() + ".")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private static String buildCompanionImagePrompt(
      String petName,
      String petDescription,
      List<String> moodTags,
      String tripSummary,
      List<IdentityVisualSlotDto> slots,
      IdentityBackgroundDto bg) {
    String style =
        "Soft editorial illustration, clean 2D character art, gentle shading, pastel and near-white palette, "
            + "product-friendly composition, one centered stylized small animal companion character, "
            + "simple supportive background, portrait-card framing, no text, no lettering, no logos, no watermarks.";
    String species = companionSpeciesLine(petName);
    String mood = String.join(", ", moodTags);
    StringBuilder accessories = new StringBuilder();
    for (IdentityVisualSlotDto sl : slots) {
      accessories.append(
          String.format(
              Locale.ROOT,
              " %s (%s): %s.",
              humanizeSlot(sl.slot()),
              sl.dimension(),
              sl.descriptor()));
    }
    return String.join(
            " ",
            style,
            "Companion role:",
            petName + ".",
            "Species and style:",
            species,
            "Mood:",
            mood + ".",
            "Companion personality:",
            petDescription,
            "Trip flavor:",
            clampSentence(tripSummary, 220),
            "Visual details:",
            accessories.toString().trim(),
            "Background setting:",
            bg.setting() + ".",
            "Background elements:",
            bg.elements() + ".",
            "Background color mood:",
            bg.colorMood() + ".")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private static String companionSpeciesLine(String petName) {
    if (petName.contains("Fox")) {
      return "Friendly fox companion with soft simplified fur shapes.";
    }
    if (petName.contains("Cat")) {
      return "Friendly cat companion with soft simplified fur shapes.";
    }
    return "Friendly bear companion with soft simplified fur shapes.";
  }

  private static String humanizeSlot(String slot) {
    return slot.replace("_", " ");
  }

  private static String clampSentence(String s, int max) {
    if (s == null) return "";
    String t = s.trim();
    if (t.length() <= max) return t;
    return t.substring(0, max).trim() + "\u2026";
  }
}
