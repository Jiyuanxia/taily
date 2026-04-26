import { getApiBaseUrl } from './apiBase'
import type { TripImage, TripResult } from '../types/contracts'

export type TripImageResponse = {
  success: boolean
  specHash?: string
  mimeType?: string
  base64?: string
  errorMessage?: string
}

function buildTripImagePrompt(trip: TripResult): string {
  const ts = trip.tripSummary
  const mustPass = trip.extractedPreferences.mustPassStops.slice(0, 4)
  const mustEat = trip.extractedPreferences.mustEat.slice(0, 3)

  // Intent: a calm "trip card" illustration representing the scene of the trip, not the pet.
  return [
    `A calm modern travel illustration for a trip card.`,
    `Destination: ${ts.destination}. Route: ${ts.routeSummary}.`,
    `Style: ${ts.styleSummary}. Duration: ${ts.durationText}. Budget: ${ts.budgetText}.`,
    mustPass.length ? `Must-pass stops: ${mustPass.join(', ')}.` : null,
    mustEat.length ? `Food focus: ${mustEat.join(', ')}.` : null,
    `Mood: soft light, warm color palette, clean shapes, slightly inspiring, no text, no lettering, no logos, no watermarks.`,
    `Composition: a single scene that evokes the trip (landmarks + street texture + subtle food cues), portrait-card framing.`,
  ]
    .filter(Boolean)
    .join(' ')
}

export async function requestTripImage(trip: TripResult, init?: { signal?: AbortSignal }): Promise<TripImage | null> {
  const prompt = buildTripImagePrompt(trip)
  const res = await fetch(`${getApiBaseUrl()}/trip/image`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ prompt }),
    signal: init?.signal,
  })
  const json = (await res.json()) as TripImageResponse
  if (!json.success || !json.specHash || !json.mimeType || !json.base64) return null
  return { specHash: json.specHash, mimeType: json.mimeType, base64: json.base64 }
}

