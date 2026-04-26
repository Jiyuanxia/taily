import { safeJsonParse, storage } from './storage'
import type { TripImage, TripResult, UserProfile } from '../types/contracts'

// In-memory (tab-local) copies so navigation never depends on localStorage timing/quota.
let memUserProfile: UserProfile | null = null
let memLastTripResult: TripResult | null = null
let memLastTripImage: TripImage | null = null

export type HomeLocation = {
  lat: number
  lon: number
  accuracyMeters?: number
  capturedAt: number
}

const KEYS = {
  hasOnboarded: 'taily.hasOnboarded',
  userProfile: 'taily.userProfile',
  lastTripResult: 'taily.lastTripResult',
  lastTripImage: 'taily.lastTripImage',
  lastTripPrompt: 'taily.lastTripPrompt',
  homeLocation: 'taily.homeLocation',
} as const

export const session = {
  isReturningUser(): boolean {
    return storage.getString(KEYS.hasOnboarded) === 'true'
  },

  markOnboarded() {
    storage.setString(KEYS.hasOnboarded, 'true')
  },

  clear() {
    memUserProfile = null
    memLastTripResult = null
    memLastTripImage = null
    storage.remove(KEYS.hasOnboarded)
    storage.remove(KEYS.userProfile)
    storage.remove(KEYS.lastTripResult)
    storage.remove(KEYS.lastTripImage)
    storage.remove(KEYS.lastTripPrompt)
    storage.remove(KEYS.homeLocation)
  },

  getUserProfile(): UserProfile | null {
    if (memUserProfile) return memUserProfile
    memUserProfile = safeJsonParse<UserProfile>(storage.getString(KEYS.userProfile))
    return memUserProfile
  },

  setUserProfile(profile: UserProfile) {
    memUserProfile = profile
    storage.setString(KEYS.userProfile, JSON.stringify(profile))
  },

  getLastTripResult(): TripResult | null {
    if (memLastTripResult) return memLastTripResult
    memLastTripResult = safeJsonParse<TripResult>(storage.getString(KEYS.lastTripResult))
    return memLastTripResult
  },

  setLastTripResult(result: TripResult) {
    // Keep full payload in-memory for the current tab (including base64 art).
    memLastTripResult = result

    // Important: keep localStorage writes small and stable. Base64 images can exceed quota and
    // silently fail (storage layer intentionally ignores errors for MVP). Portraits are cached
    // separately by specHash; the trip image is stored separately as well.
    const pruned: TripResult = { ...result }
    delete (pruned as Partial<TripResult>).companionPortrait
    storage.setString(KEYS.lastTripResult, JSON.stringify(pruned))
  },

  getLastTripImage(): TripImage | null {
    if (memLastTripImage) return memLastTripImage
    memLastTripImage = safeJsonParse<TripImage>(storage.getString(KEYS.lastTripImage))
    return memLastTripImage
  },

  setLastTripImage(img: TripImage) {
    memLastTripImage = img
    storage.setString(KEYS.lastTripImage, JSON.stringify(img))
  },

  getLastTripPrompt(): string | null {
    const v = storage.getString(KEYS.lastTripPrompt)
    return v && v.trim().length > 0 ? v : null
  },

  setLastTripPrompt(prompt: string) {
    storage.setString(KEYS.lastTripPrompt, prompt)
  },

  getHomeLocation(): HomeLocation | null {
    return safeJsonParse<HomeLocation>(storage.getString(KEYS.homeLocation))
  },

  setHomeLocation(loc: HomeLocation) {
    storage.setString(KEYS.homeLocation, JSON.stringify(loc))
  },
}

