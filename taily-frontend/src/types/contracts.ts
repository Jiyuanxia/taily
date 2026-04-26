export type DimensionKey =
  | 'budget'
  | 'pace'
  | 'foodFocus'
  | 'sightseeing'
  | 'comfort'
  | 'exploration'

export type DimensionScores = Record<DimensionKey, 1 | 2 | 3 | 4 | 5>

export type OnboardingAnswers = Record<DimensionKey, string>

export type Archetype = {
  name: string
  description: string
}

export type AvatarConfig = {
  styleTone: string
  energyLevel: string
  vibe: string
}

export type DimensionLevelStr = 'low' | 'medium' | 'high'

export type IdentityBackgroundSpec = {
  setting: string
  elements: string
  colorMood: string
}

export type IdentityVisualSlot = {
  slot: string
  dimension: DimensionKey
  level: DimensionLevelStr
  descriptor: string
}

export type TravelerIdentitySpec = {
  kind: 'traveler'
  archetypeId: string
  archetypeName: string
  archetypeDescription: string
  moodTags: string[]
  summary: string
  dimensionLevels: Record<DimensionKey, DimensionLevelStr>
  visualSlots: IdentityVisualSlot[]
  background: IdentityBackgroundSpec
  visualTags: string[]
  imagePrompt: string
  /** Server v1+; required for portrait cache. */
  specHash?: string
}

export type TripCompanionSpec = {
  kind: 'trip_companion_pet'
  archetypeId: string
  archetypeName: string
  archetypeDescription: string
  moodTags: string[]
  summary: string
  dimensionLevels: Record<DimensionKey, DimensionLevelStr>
  visualSlots: IdentityVisualSlot[]
  background: IdentityBackgroundSpec
  visualTags: string[]
  imagePrompt: string
  specHash?: string
}

export type IdentityPortrait = {
  specHash: string
  mimeType: string
  base64: string
}

export type TripImage = {
  specHash: string
  mimeType: string
  base64: string
}

export type UserProfile = {
  dimensionScores: DimensionScores
  profileSummary: string
  travelerArchetype: Archetype
  avatarConfig: AvatarConfig
  /** Present after identity v1; older cached profiles may omit. */
  travelerIdentitySpec?: TravelerIdentitySpec
  /** Cached portrait for current travelerIdentitySpec (session + optional cache). */
  travelerPortrait?: IdentityPortrait
}

export type TripProfile = {
  dimensionScores: DimensionScores
}

export type TripSummary = {
  origin: string
  destination: string
  durationText: string
  budgetText: string
  routeSummary: string
  styleSummary: string
}

export type ExtractedPreferences = {
  mustPassStops: string[]
  mustEat: string[]
  mustSeeOrDo: string[]
  lodgingPreference: string
  budgetPreference: string
  pacePreference: string
}

export type OptionalHighlight = {
  name: string
  reason: string
}

export type ItineraryDay = {
  day: number
  title: string
  area: string
  activities: string[]
  vibeNote: string
  mustHaveCoverage: string[]
  optionalHighlights: OptionalHighlight[]
}

export type WorthConsidering = {
  name: string
  reason: string
}

export type FitAnalysis = {
  overallFit: {
    score: number
    label: string
  }
  topMatches: string[]
  topMismatches: string[]
  summary: string
}

export type TripResult = {
  tripSummary: TripSummary
  extractedPreferences: ExtractedPreferences
  itinerary: ItineraryDay[]
  worthConsidering: WorthConsidering[]
  tripProfile: TripProfile
  fitAnalysis: FitAnalysis
  travelerArchetype: Archetype
  tripCompanionPet: Archetype
  /** Present for API v1+; omitted in older cached payloads. */
  travelerIdentitySpec?: TravelerIdentitySpec
  tripCompanionSpec?: TripCompanionSpec
  /** Cached portrait for current tripCompanionSpec. */
  companionPortrait?: IdentityPortrait
}

export type ProfileCreateRequest = {
  onboardingAnswers: OnboardingAnswers
}

export type ProfileCreateResponse = {
  userProfile: UserProfile
}

export type TripGenerateWithFitRequest = {
  userProfile: {
    dimensionScores: DimensionScores
    /** When set, traveler identity summary matches the stored baseline profile. */
    profileSummary?: string
  }
  tripRequest: {
    prompt: string
  }
  context?: {
    origin?: {
      value: string
      source?: string
    }
  }
}

export type TripRefineRequest = {
  userProfile: {
    dimensionScores: DimensionScores
    profileSummary?: string
  }
  currentTrip: {
    tripSummary: TripSummary
    itinerary: ItineraryDay[]
    tripProfile: {
      dimensionScores: DimensionScores
    }
  }
  refinement: {
    dimensionAdjustments: Partial<DimensionScores>
    prompt: string
  }
}

