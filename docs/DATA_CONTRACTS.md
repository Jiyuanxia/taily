# Data Contracts

## Schema design principle

Taily allows users to describe trips in flexible natural language, but the backend should normalize both user preferences and trip outputs into a stable structure.

The purpose of the schema is to:
- keep frontend rendering predictable
- make AI outputs more controllable
- allow the same product logic to support multiple client surfaces in the future
- preserve room for richer profile evolution later without overloading the MVP

---

## 1. Onboarding answers (profile create request)

### Purpose
Capture the user’s explicit travel preferences from the onboarding flow.

### Structure

    {
      "onboardingAnswers": {
        "budget": "balanced",
        "pace": "relaxed",
        "foodFocus": "high",
        "sightseeing": "medium",
        "comfort": "high",
        "exploration": "medium"
      }
    }

### Notes
- These values are raw user selections from the onboarding UI.
- They are human-readable and simple.
- The backend is responsible for normalizing them into dimension scores.

---

## 2. Profile creation response

### Purpose
Return a baseline user profile that can be used across generation, fit analysis, and refinement.

### Structure

    {
      "userProfile": {
        "dimensionScores": {
          "budget": 3,
          "pace": 2,
          "foodFocus": 5,
          "sightseeing": 3,
          "comfort": 5,
          "exploration": 3
        },
        "profileSummary": "You prefer comfortable, slower-paced trips with a strong focus on food and a moderate interest in exploration.",
        "travelerArchetype": {
          "name": "Slow Food Dreamer",
          "description": "A cozy traveler who values good food, gentle pacing, and comfortable experiences."
        },
        "avatarConfig": {
          "styleTone": "warm",
          "energyLevel": "low-medium",
          "vibe": "calm and refined"
        },
        "travelerIdentitySpec": {
          "kind": "traveler",
          "archetypeId": "slow_food_dreamer",
          "archetypeName": "Slow Food Dreamer",
          "archetypeDescription": "A cozy traveler who values good food, gentle pacing, and comfortable experiences.",
          "moodTags": ["warm", "cozy", "relaxed"],
          "summary": "You prefer comfortable, slower-paced trips with a strong focus on food and a moderate interest in exploration.",
          "dimensionLevels": { "budget": "medium", "pace": "low", "foodFocus": "high", "sightseeing": "medium", "comfort": "high", "exploration": "medium" },
          "visualSlots": [],
          "background": { "setting": "string", "elements": "string", "colorMood": "string" },
          "visualTags": ["warm", "cozy", "food-led"],
          "imagePrompt": "string (stable illustration prompt; no image bytes in MVP v1)",
          "specHash": "64-char lowercase hex SHA-256 fingerprint of the spec used for portrait cache keys"
        }
      }
    }

### Notes
- Dimension scores use a **1 to 5** scale.
- `profileSummary` is a short natural-language summary for the user.
- `travelerArchetype` is lightweight and descriptive.
- `avatarConfig` is intentionally simple for MVP and should support type-based visual generation, not detailed character customization.
- **`travelerIdentitySpec` (v1)** is a deterministic identity card package: canonical archetype, per-dimension `low` \| `medium` \| `high`, symbolic visual slot copy, background hints, tags, an **`imagePrompt`** string, and **`specHash`** (SHA-256 over a canonical string) for deduplicating portrait generation. `visualSlots` is an ordered array of six slot objects (`slot`, `dimension`, `level`, `descriptor`); the example above omits the full array for brevity in this doc.

---

## 2b. Identity portrait (optional image step)

### Purpose
Generate a **512×512** raster from a spec’s `imagePrompt`. The web client calls this **after** profile/trip/refine responses when it needs a new portrait; rendered bytes are **not** embedded in profile/trip JSON.

### `POST /identity/portrait`

Request:

    {
      "kind": "traveler",
      "prompt": "…imagePrompt from travelerIdentitySpec…",
      "specHash": "…same as travelerIdentitySpec.specHash…"
    }

Response (always HTTP 200; check `success`):

    {
      "success": true,
      "mimeType": "image/png",
      "base64": "…"
    }

or on failure / disabled OpenAI:

    {
      "success": false,
      "mimeType": null,
      "base64": null,
      "errorMessage": "OpenAI integration is disabled."
    }

### Notes
- `kind` is `traveler` or `trip_companion_pet` (matches identity spec `kind`).
- Prompts are truncated server-side to **1000 characters** (DALL-E 2 limit).
- Default model/size are configurable via `taily.openai.image-model` and `taily.openai.portrait-size` (default `dall-e-2` / `512x512`).
- The client should **cache** portraits by `specHash` and avoid repeat calls when the hash is unchanged.

---

## 3. Trip generate-with-fit request

### Purpose
Send the user profile and current trip request to the backend.

### Structure

    {
      "userProfile": {
        "dimensionScores": {
          "budget": 3,
          "pace": 2,
          "foodFocus": 5,
          "sightseeing": 3,
          "comfort": 5,
          "exploration": 3
        },
        "profileSummary": "You prefer comfortable, slower-paced trips with a strong focus on food and a moderate interest in exploration."
      },
      "tripRequest": {
        "prompt": "I want a 5-day Tokyo trip in June, with good food, one Disney day, and a mid-range hotel."
      },
      "context": {
        "origin": {
          "value": "New York City",
          "source": "default"
        }
      }
    }

### Notes
- The frontend keeps a **single main natural-language input field**.
- The backend is responsible for extracting structure from the prompt.
- **`userProfile.profileSummary`** is optional on trip requests; when the client sends it (from the stored baseline profile), traveler `travelerIdentitySpec.summary` and prompts align with onboarding copy. If omitted, the backend synthesizes a one-line summary from scores.
- `origin` is important, but MVP may allow it to come from:
  - user input
  - device location
  - default value
  - missing or unknown state later
- `source` is optional but helpful for future evolution.

---

## 4. Generate-with-fit response

### Purpose
Return everything needed for the result page in one stable response.

### Implementation note (MVP)
- The backend may use OpenAI to generate trip fields (preferences, itinerary, summaries, suggestions, fit summary),
  but it must still return this exact structure with stable, frontend-friendly values.
- If AI output is invalid or incomplete, the backend should fall back to a deterministic first-pass response.

### Structure

    {
      "tripSummary": {
        "origin": "New York City",
        "destination": "Tokyo",
        "durationText": "5 days",
        "budgetText": "Mid-range",
        "routeSummary": "Tokyo with one Disney day and food-focused stops",
        "styleSummary": "A comfortable foodie trip with a relaxed overall pace"
      },
      "extractedPreferences": {
        "mustPassStops": ["Tokyo DisneySea"],
        "mustEat": ["Sushi", "Izakaya dinner"],
        "mustSeeOrDo": ["One Disney day"],
        "lodgingPreference": "Mid-range hotel",
        "budgetPreference": "Balanced / mid-range",
        "pacePreference": "Relaxed"
      },
      "itinerary": [
        {
          "day": 1,
          "title": "Easy arrival and Shibuya evening",
          "area": "Shibuya",
          "activities": [
            "Hotel check-in",
            "Light walk in Shibuya",
            "Dinner at a local izakaya"
          ],
          "vibeNote": "A relaxed start with light movement",
          "mustHaveCoverage": ["Izakaya dinner"],
          "optionalHighlights": [
            {
              "name": "Shibuya Sky",
              "reason": "A scenic optional add-on if you want a stronger first-night city view"
            }
          ]
        },
        {
          "day": 2,
          "title": "Disney day",
          "area": "Tokyo DisneySea",
          "activities": [
            "Full-day Disney visit",
            "Evening return with casual dinner"
          ],
          "vibeNote": "High-energy day compared with the rest of the trip",
          "mustHaveCoverage": ["One Disney day"],
          "optionalHighlights": [
            {
              "name": "Ikspiari",
              "reason": "A good nearby dining option if you want a slower end to the day"
            }
          ]
        }
      ],
      "worthConsidering": [
        {
          "name": "Kappabashi Street",
          "reason": "A strong optional stop if you enjoy food culture beyond restaurants"
        },
        {
          "name": "Daikanyama",
          "reason": "A softer, more refined neighborhood that fits your comfort and pace preferences"
        }
      ],
      "tripProfile": {
        "dimensionScores": {
          "budget": 3,
          "pace": 3,
          "foodFocus": 5,
          "sightseeing": 3,
          "comfort": 4,
          "exploration": 3
        }
      },
      "fitAnalysis": {
        "overallFit": {
          "score": 82,
          "label": "Strong Fit"
        },
        "topMatches": ["Food Focus", "Comfort"],
        "topMismatches": ["Pace"],
        "summary": "This trip strongly matches your food interest and comfort preference, but includes one denser day than your usual pace."
      },
      "travelerArchetype": {
        "name": "Slow Food Dreamer",
        "description": "A cozy traveler who values good food, gentle pacing, and comfortable experiences."
      },
      "tripCompanionPet": {
        "name": "Cozy Travel Bear",
        "description": "A warm travel buddy who makes the trip feel safe, soft, and comfortable."
      },
      "travelerIdentitySpec": {
        "kind": "traveler",
        "archetypeId": "slow_food_dreamer",
        "archetypeName": "Slow Food Dreamer",
        "archetypeDescription": "A cozy traveler who values good food, gentle pacing, and comfortable experiences.",
        "moodTags": ["warm", "cozy", "relaxed"],
        "summary": "string",
        "dimensionLevels": { "budget": "medium", "pace": "low", "foodFocus": "high", "sightseeing": "medium", "comfort": "high", "exploration": "medium" },
        "visualSlots": [],
        "background": { "setting": "string", "elements": "string", "colorMood": "string" },
        "visualTags": ["warm", "cozy", "relaxed"],
        "imagePrompt": "string"
      },
      "tripCompanionSpec": {
        "kind": "trip_companion_pet",
        "archetypeId": "cozy_travel_bear",
        "archetypeName": "Cozy Travel Bear",
        "archetypeDescription": "A warm travel buddy who makes the trip feel safe, soft, and comfortable.",
        "moodTags": ["warm", "gentle", "reliable"],
        "summary": "A comfortable foodie trip with a relaxed overall pace",
        "dimensionLevels": { "budget": "medium", "pace": "medium", "foodFocus": "high", "sightseeing": "medium", "comfort": "high", "exploration": "medium" },
        "visualSlots": [],
        "background": { "setting": "string", "elements": "string", "colorMood": "string" },
        "visualTags": ["warm", "gentle", "reliable"],
        "imagePrompt": "string"
      }
    }

---

## 5. Notes on generate response structure

### Why `tripSummary` exists
This gives the page an immediate “what kind of trip is this” layer before the user reads the full itinerary.

### Why `extractedPreferences` exists
This shows the user that Taily actually understood the most important parts of the request.

### Why `itinerary` is day-based
This balances detail and usability for MVP:
- more concrete than a paragraph
- lighter than full morning / afternoon / evening breakdowns

### Why both `optionalHighlights` and `worthConsidering` exist
Because the product should support:
- local day-level expansion opportunities
- global trip-level expansion opportunities

### Why `fitAnalysis` stays concise
MVP should emphasize:
- overall fit
- top matches
- top mismatches
- one clear explanation

It should not overload the page with per-dimension paragraphs yet.

### Identity v1 (`travelerIdentitySpec`, `tripCompanionSpec`)
- Returned on **generate-with-fit** and **refine** responses (same shape as profile `travelerIdentitySpec` for the traveler side).
- **Traveler archetype** is always one of: *Slow Food Dreamer*, *Balanced City Explorer*, *Fast Adventure Chaser* (deterministic from user dimension scores).
- **Trip companion pet** display is always one of: *Lazy Carpet Cat*, *Curious Scout Fox*, *Cozy Travel Bear* (deterministic from **trip** dimension scores). Model-generated pet labels are not used for these canonical names.
- Dimension bands for slot copy: **1–2 → low**, **3 → medium**, **4–5 → high**.
- **`imagePrompt`** is included for both specs; MVP does not return rendered image bytes.

---

## 6. Refine request

### Purpose
Allow users to adjust the generated trip using both structured and natural-language refinement.

### Implementation note (MVP)
- The refine response should use the **same structure** as the generate-with-fit response.
- The backend should treat refinement as **replace current result with a full updated result**.
- For the first MVP pass, refinement may preserve most of the existing itinerary and apply smaller, understandable adjustments.

### Structure

    {
      "userProfile": {
        "dimensionScores": {
          "budget": 3,
          "pace": 2,
          "foodFocus": 5,
          "sightseeing": 3,
          "comfort": 5,
          "exploration": 3
        },
        "profileSummary": "You prefer comfortable, slower-paced trips with a strong focus on food and a moderate interest in exploration."
      },
      "currentTrip": {
        "tripSummary": {
          "origin": "New York City",
          "destination": "Tokyo",
          "durationText": "5 days",
          "budgetText": "Mid-range",
          "routeSummary": "Tokyo with one Disney day and food-focused stops",
          "styleSummary": "A comfortable foodie trip with a relaxed overall pace"
        },
        "itinerary": [],
        "tripProfile": {
          "dimensionScores": {
            "budget": 3,
            "pace": 3,
            "foodFocus": 5,
            "sightseeing": 3,
            "comfort": 4,
            "exploration": 3
          }
        }
      },
      "refinement": {
        "dimensionAdjustments": {
          "pace": 2,
          "comfort": 5
        },
        "prompt": "Keep the Disney day, but make the overall trip feel less rushed and a bit more comfortable."
      }
    }

### Notes
- `dimensionAdjustments` should only include the dimensions the user actually changed.
- This keeps the request lighter and makes the logic easier to explain.
- `prompt` supports a richer re-planning experience beyond simple button clicks.

---

## 7. Refine response

### Purpose
Return a fully updated trip result after refinement.

### Structure
Use the **same structure as the generate-with-fit response**.

### Notes
- The refinement response should be **full and self-contained**.
- This keeps frontend rendering simple.
- The frontend can treat refinement as “replace current result with new result.”

---

## 8. Score conventions

### Dimension score scale
All dimension scores use:
- **1 = low**
- **2 = low-medium**
- **3 = medium**
- **4 = medium-high**
- **5 = high**

### Fit score
`overallFit.score` uses:
- **0 to 100 integer scale**

### Fit label
Suggested first-pass labels:
- `80 to 100` → Strong Fit
- `60 to 79` → Moderate Fit
- `Below 60` → Weak Fit

These ranges can be tuned later.

### Identity spec level bands (parallel to numeric scores)
Used when mapping each dimension into `travelerIdentitySpec` / `tripCompanionSpec` slot copy:
- **1–2** → `low`
- **3** → `medium`
- **4–5** → `high`

---

## 9. MVP-only simplifications

For the MVP:
- `origin` may use a default value if unavailable
- `avatarConfig` stays lightweight
- `travelerArchetype` and `tripCompanionPet` remain type-based (canonical v1 names; see §5 identity notes)
- `travelerIdentitySpec` / `tripCompanionSpec` provide structured card data and `imagePrompt` only (no hosted portrait asset in v1)
- no persistent profile updates are written back
- no profile evolution events are returned
- no route coordinates are required yet
- map rendering can start from place names rather than fully structured geolocation data

---

## 10. Future extension space

The current schema intentionally leaves room for future expansion, including:
- richer avatar parameters
- profile evolution events
- profile versioning
- map coordinates and route geometry
- stronger recommendation reasoning
- per-dimension fit explanations
- multi-version trip comparison

These should be added later without changing the MVP core structure too much.