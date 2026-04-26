import { safeJsonParse, storage } from './storage'

const LS_KEY = 'taily.portraitCache.v1'
const MAX_ENTRIES = 4

type PortraitEntry = {
  mimeType: string
  base64: string
  storedAt: number
}

type CacheStore = {
  /** LRU order: oldest first, newest last */
  order: string[]
  entries: Record<string, PortraitEntry>
}

function readStore(): CacheStore {
  const raw = storage.getString(LS_KEY)
  const parsed = safeJsonParse<CacheStore>(raw)
  if (parsed && Array.isArray(parsed.order) && parsed.entries && typeof parsed.entries === 'object') {
    return { order: parsed.order, entries: parsed.entries }
  }
  return { order: [], entries: {} }
}

function writeStore(store: CacheStore) {
  storage.setString(LS_KEY, JSON.stringify(store))
}

function isPinned(key: string, pins: { traveler: string | null; companion: string | null }) {
  return key === pins.traveler || key === pins.companion
}

function evictIfNeeded(store: CacheStore, pins: { traveler: string | null; companion: string | null }) {
  while (store.order.length > MAX_ENTRIES) {
    let evicted = false
    for (let i = 0; i < store.order.length; i++) {
      const k = store.order[i]!
      if (!isPinned(k, pins)) {
        store.order.splice(i, 1)
        delete store.entries[k]
        evicted = true
        break
      }
    }
    if (!evicted) break
  }
}

export function getCachedPortrait(specHash: string): PortraitEntry | null {
  const store = readStore()
  return store.entries[specHash] ?? null
}

/**
 * Stores a portrait under specHash. Evicts oldest unpinned entries when over capacity.
 * Pass current traveler + companion spec hashes so they are never evicted.
 */
export function putCachedPortrait(
  specHash: string,
  data: { mimeType: string; base64: string },
  pins: { traveler: string | null; companion: string | null },
) {
  const store = readStore()
  const idx = store.order.indexOf(specHash)
  if (idx >= 0) store.order.splice(idx, 1)
  store.order.push(specHash)
  store.entries[specHash] = { ...data, storedAt: Date.now() }
  evictIfNeeded(store, pins)
  writeStore(store)
}
