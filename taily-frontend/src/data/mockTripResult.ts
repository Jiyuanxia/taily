import type { TripResult } from '../types/contracts'

const mockTravelerIdentity = {
  kind: 'traveler' as const,
  archetypeId: 'balanced_city_explorer',
  archetypeName: 'Balanced City Explorer',
  archetypeDescription:
    'A balanced traveler who enjoys discovering cities, local highlights, and a well-paced itinerary.',
  moodTags: ['balanced', 'curious', 'urban'],
  summary:
    'You prefer balanced-pace, mid-range trips with a balanced mix of experiences.',
  dimensionLevels: {
    budget: 'medium' as const,
    pace: 'medium' as const,
    foodFocus: 'medium' as const,
    sightseeing: 'medium' as const,
    comfort: 'medium' as const,
    exploration: 'medium' as const,
  },
  visualSlots: [
    { slot: 'shoes', dimension: 'pace' as const, level: 'medium' as const, descriptor: 'comfortable travel sneakers' },
    { slot: 'bag', dimension: 'budget' as const, level: 'medium' as const, descriptor: 'neat travel backpack' },
    { slot: 'hand_item', dimension: 'foodFocus' as const, level: 'medium' as const, descriptor: 'snack or dessert' },
    {
      slot: 'head_or_viewing_accessory',
      dimension: 'sightseeing' as const,
      level: 'medium' as const,
      descriptor: 'camera charm',
    },
    { slot: 'outerwear', dimension: 'comfort' as const, level: 'medium' as const, descriptor: 'casual jacket' },
    {
      slot: 'map_or_compass_item',
      dimension: 'exploration' as const,
      level: 'medium' as const,
      descriptor: 'folded city map',
    },
  ],
  background: {
    setting: 'clean city street scene',
    elements: 'light building silhouettes, route lines, subtle city signage',
    colorMood: 'light gray, muted blue, pale sage',
  },
  visualTags: ['balanced', 'curious', 'urban', 'balanced-city-explorer'],
  imagePrompt:
    'Soft editorial illustration, clean 2D character art, gentle shading, pastel and near-white palette, one centered friendly stylized adult traveler character, city explorer mood, comfortable travel sneakers, neat travel backpack, snack or dessert, camera charm, casual jacket, folded city map, clean city street background, no text.',
  specHash: '0'.repeat(64),
}

const mockTripCompanion = {
  kind: 'trip_companion_pet' as const,
  archetypeId: 'cozy_travel_bear',
  archetypeName: 'Cozy Travel Bear',
  archetypeDescription:
    'A warm travel buddy who makes the trip feel safe, soft, and comfortable.',
  moodTags: ['warm', 'gentle', 'reliable'],
  summary: 'A comfortable foodie trip with a relaxed overall pace',
  dimensionLevels: {
    budget: 'medium' as const,
    pace: 'medium' as const,
    foodFocus: 'high' as const,
    sightseeing: 'medium' as const,
    comfort: 'high' as const,
    exploration: 'medium' as const,
  },
  visualSlots: [
    { slot: 'movement_style', dimension: 'pace' as const, level: 'medium' as const, descriptor: 'walking lightly' },
    { slot: 'gear_quality', dimension: 'budget' as const, level: 'medium' as const, descriptor: 'tidy travel gear' },
    { slot: 'food_prop', dimension: 'foodFocus' as const, level: 'high' as const, descriptor: 'prominent food item' },
    { slot: 'head_accessory', dimension: 'sightseeing' as const, level: 'medium' as const, descriptor: 'travel cap' },
    {
      slot: 'comfort_item',
      dimension: 'comfort' as const,
      level: 'high' as const,
      descriptor: 'plush blanket or comfy cushion',
    },
    { slot: 'explorer_item', dimension: 'exploration' as const, level: 'medium' as const, descriptor: 'folded map' },
  ],
  background: {
    setting: 'warm travel comfort backdrop',
    elements: 'soft seating shapes, warm interior travel hints, steam-like accents',
    colorMood: 'warm cream, soft tan, muted rose',
  },
  visualTags: ['warm', 'gentle', 'reliable', 'soft-trip', 'cozy-travel-bear'],
  imagePrompt:
    'Soft editorial illustration, clean 2D character art, gentle shading, pastel and near-white palette, one centered stylized small animal companion character, friendly bear companion with soft simplified fur shapes, walking lightly, tidy travel gear, prominent food item, travel cap, plush blanket or comfy cushion, folded map, warm travel comfort backdrop, no text.',
  specHash: 'f'.repeat(64),
}

export const MOCK_TRIP_RESULT: TripResult = {
  tripSummary: {
    origin: 'New York City',
    destination: 'Tokyo',
    durationText: '5 days',
    budgetText: 'Mid-range',
    routeSummary: 'Tokyo with one Disney day and food-focused stops',
    styleSummary: 'A comfortable foodie trip with a relaxed overall pace',
  },
  extractedPreferences: {
    mustPassStops: ['Tokyo DisneySea'],
    mustEat: ['Sushi', 'Izakaya dinner'],
    mustSeeOrDo: ['One Disney day'],
    lodgingPreference: 'Mid-range hotel',
    budgetPreference: 'Balanced / mid-range',
    pacePreference: 'Relaxed',
  },
  itinerary: [
    {
      day: 1,
      title: 'Easy arrival and Shibuya evening',
      area: 'Shibuya',
      activities: ['Hotel check-in', 'Light walk in Shibuya', 'Dinner at a local izakaya'],
      vibeNote: 'A relaxed start with light movement',
      mustHaveCoverage: ['Izakaya dinner'],
      optionalHighlights: [
        {
          name: 'Shibuya Sky',
          reason: 'A scenic optional add-on if you want a stronger first-night city view',
        },
      ],
    },
    {
      day: 2,
      title: 'Disney day',
      area: 'Tokyo DisneySea',
      activities: ['Full-day Disney visit', 'Evening return with casual dinner'],
      vibeNote: 'High-energy day compared with the rest of the trip',
      mustHaveCoverage: ['One Disney day'],
      optionalHighlights: [
        {
          name: 'Ikspiari',
          reason: 'A good nearby dining option if you want a slower end to the day',
        },
      ],
    },
  ],
  worthConsidering: [
    {
      name: 'Kappabashi Street',
      reason: 'A strong optional stop if you enjoy food culture beyond restaurants',
    },
    {
      name: 'Daikanyama',
      reason: 'A softer, more refined neighborhood that fits your comfort and pace preferences',
    },
  ],
  tripProfile: {
    dimensionScores: {
      budget: 3,
      pace: 3,
      foodFocus: 5,
      sightseeing: 3,
      comfort: 4,
      exploration: 3,
    },
  },
  fitAnalysis: {
    overallFit: { score: 82, label: 'Strong Fit' },
    topMatches: ['Food Focus', 'Comfort'],
    topMismatches: ['Pace'],
    summary:
      'This trip strongly matches your food interest and comfort preference, but includes one denser day than your usual pace.',
  },
  travelerArchetype: {
    name: 'Balanced City Explorer',
    description:
      'A balanced traveler who enjoys discovering cities, local highlights, and a well-paced itinerary.',
  },
  tripCompanionPet: {
    name: 'Cozy Travel Bear',
    description: 'A warm travel buddy who makes the trip feel safe, soft, and comfortable.',
  },
  travelerIdentitySpec: mockTravelerIdentity,
  tripCompanionSpec: mockTripCompanion,
}
