import type {
  ProfileCreateRequest,
  ProfileCreateResponse,
  TripGenerateWithFitRequest,
  TripRefineRequest,
  TripResult,
} from '../types/contracts'

import { getApiBaseUrl } from './apiBase'

export { getApiBaseUrl }

export async function createProfile(
  request: ProfileCreateRequest,
  init?: { signal?: AbortSignal }
): Promise<ProfileCreateResponse> {
  const res = await fetch(`${getApiBaseUrl()}/profile/create`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(request),
    signal: init?.signal,
  })

  if (!res.ok) {
    throw new Error(`Profile create failed (${res.status})`)
  }

  return (await res.json()) as ProfileCreateResponse
}

export async function generateWithFit(
  request: TripGenerateWithFitRequest,
  init?: { signal?: AbortSignal }
): Promise<TripResult> {
  const res = await fetch(`${getApiBaseUrl()}/trip/generate-with-fit`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(request),
    signal: init?.signal,
  })

  if (!res.ok) {
    throw new Error(`Trip generate failed (${res.status})`)
  }

  return (await res.json()) as TripResult
}

export async function refineTrip(
  request: TripRefineRequest,
  init?: { signal?: AbortSignal }
): Promise<TripResult> {
  const res = await fetch(`${getApiBaseUrl()}/trip/refine`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(request),
    signal: init?.signal,
  })

  if (!res.ok) {
    throw new Error(`Trip refine failed (${res.status})`)
  }

  return (await res.json()) as TripResult
}

