import { getApiBaseUrl } from './apiBase'
import { getCachedPortrait, putCachedPortrait } from './identityPortraitCache'
import type { IdentityPortrait, TripResult, UserProfile } from '../types/contracts'

export type PortraitPins = { traveler: string | null; companion: string | null }

export type IdentityPortraitResponse = {
  success: boolean
  mimeType?: string
  base64?: string
  errorMessage?: string
}

export async function requestIdentityPortrait(
  body: {
    kind: 'traveler' | 'trip_companion_pet'
    prompt: string
    specHash: string
  },
  init?: { signal?: AbortSignal },
): Promise<IdentityPortraitResponse> {
  const res = await fetch(`${getApiBaseUrl()}/identity/portrait`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(body),
    signal: init?.signal,
  })
  const json = (await res.json()) as IdentityPortraitResponse
  return json
}

function portraitPinsFromSession(trip: TripResult | null, profile: UserProfile | null): PortraitPins {
  return {
    traveler: profile?.travelerIdentitySpec?.specHash ?? null,
    companion: trip?.tripCompanionSpec?.specHash ?? null,
  }
}

export async function ensureTravelerPortrait(
  profile: UserProfile,
  trip: TripResult | null,
  init?: { signal?: AbortSignal },
): Promise<UserProfile> {
  const spec = profile.travelerIdentitySpec
  if (!spec?.specHash) return profile

  const pins = portraitPinsFromSession(trip, profile)
  const cached = getCachedPortrait(spec.specHash)
  if (cached) {
    return {
      ...profile,
      travelerPortrait: { specHash: spec.specHash, mimeType: cached.mimeType, base64: cached.base64 },
    }
  }

  const res = await requestIdentityPortrait(
    { kind: 'traveler', prompt: spec.imagePrompt, specHash: spec.specHash },
    init,
  )
  if (!res.success || !res.mimeType || !res.base64) return profile

  putCachedPortrait(spec.specHash, { mimeType: res.mimeType, base64: res.base64 }, pins)
  return {
    ...profile,
    travelerPortrait: { specHash: spec.specHash, mimeType: res.mimeType, base64: res.base64 },
  }
}

export async function ensureCompanionPortrait(
  trip: TripResult,
  profile: UserProfile | null,
  init?: { signal?: AbortSignal },
): Promise<TripResult> {
  const spec = trip.tripCompanionSpec
  if (!spec?.specHash) return trip

  const pins = portraitPinsFromSession(trip, profile)
  const cached = getCachedPortrait(spec.specHash)
  if (cached) {
    return {
      ...trip,
      companionPortrait: { specHash: spec.specHash, mimeType: cached.mimeType, base64: cached.base64 },
    }
  }

  const res = await requestIdentityPortrait(
    { kind: 'trip_companion_pet', prompt: spec.imagePrompt, specHash: spec.specHash },
    init,
  )
  if (!res.success || !res.mimeType || !res.base64) return trip

  putCachedPortrait(spec.specHash, { mimeType: res.mimeType, base64: res.base64 }, pins)
  return {
    ...trip,
    companionPortrait: { specHash: spec.specHash, mimeType: res.mimeType, base64: res.base64 },
  }
}

/** After refine: regenerate companion portrait only when spec hash changed; preserve prior portrait when unchanged. */
export async function maybeRefreshCompanionPortrait(
  previousHash: string,
  trip: TripResult,
  profile: UserProfile | null,
  init?: {
    signal?: AbortSignal
    previousPortrait?: IdentityPortrait | null
  },
): Promise<TripResult> {
  const nextHash = trip.tripCompanionSpec?.specHash
  if (!nextHash) return trip
  if (nextHash === previousHash) {
    const prev = init?.previousPortrait
    if (prev && prev.specHash === nextHash) return { ...trip, companionPortrait: prev }
    return trip
  }
  return ensureCompanionPortrait(trip, profile, init?.signal ? { signal: init.signal } : undefined)
}

