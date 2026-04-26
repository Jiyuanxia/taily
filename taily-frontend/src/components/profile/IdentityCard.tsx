import { Card } from '../ui/Card'
import type { IdentityPortrait, TravelerIdentitySpec, TripCompanionSpec } from '../../types/contracts'

function titleCaseSlot(slot: string) {
  return slot.replace(/_/g, ' ')
}

export type IdentityCardSpec = TravelerIdentitySpec | TripCompanionSpec

export function IdentityCard({
  title,
  spec,
  portrait,
  meta,
  fallbackName,
  fallbackDescription,
}: {
  title: string
  spec?: IdentityCardSpec
  portrait?: IdentityPortrait | null
  meta?: { label: string; value: string }[]
  fallbackName?: string
  fallbackDescription?: string
}) {
  const name = spec?.archetypeName ?? fallbackName ?? 'Identity'
  const description = spec?.archetypeDescription ?? fallbackDescription ?? ''
  const summary = spec?.summary
  const mood = spec?.moodTags?.length ? spec.moodTags.join(' · ') : null
  const isPet = spec?.kind === 'trip_companion_pet'
  const portraitOk =
    portrait && spec?.specHash && portrait.specHash === spec.specHash && portrait.base64?.length > 0
  const portraitSrc = portraitOk
    ? `data:${portrait.mimeType || 'image/png'};base64,${portrait.base64}`
    : null

  return (
    <Card>
      <div className="stack" style={{ gap: 12 }}>
        <div className="small muted">{title}</div>
        <div className="row" style={{ gap: 14, alignItems: 'stretch' }}>
          <div
            aria-hidden="true"
            style={{
              width: 108,
              minWidth: 108,
              minHeight: 108,
              borderRadius: 18,
              border: '1px solid var(--border)',
              background: isPet
                ? 'linear-gradient(145deg, rgba(20, 184, 166, 0.22), rgba(127, 127, 140, 0.06))'
                : 'linear-gradient(140deg, rgba(110, 86, 207, 0.22), rgba(127, 127, 140, 0.06))',
              display: 'grid',
              placeItems: 'center',
              padding: portraitSrc ? 0 : 10,
              overflow: 'hidden',
            }}
          >
            {portraitSrc ? (
              <img
                src={portraitSrc}
                alt=""
                style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
              />
            ) : (
              <>
                <div
                  className="small muted"
                  style={{ textAlign: 'center', lineHeight: 1.2, fontWeight: 650, color: 'var(--text)' }}
                >
                  {isPet ? 'Companion art' : 'Traveler art'}
                </div>
                <div className="small muted" style={{ textAlign: 'center', lineHeight: 1.15, marginTop: 4 }}>
                  Portrait
                  <br />
                  <span style={{ fontWeight: 500, fontSize: 11 }}>not available</span>
                </div>
              </>
            )}
          </div>

          <div style={{ flex: '1 1 auto' }}>
            <div style={{ fontWeight: 750, letterSpacing: '-0.02em' }}>{name}</div>
            <div className="muted" style={{ marginTop: 4 }}>
              {description}
            </div>
            {summary ? (
              <div className="small muted" style={{ marginTop: 8, lineHeight: 1.45 }}>
                {summary}
              </div>
            ) : null}
            {mood ? (
              <div className="small muted" style={{ marginTop: 6 }}>
                <span style={{ fontWeight: 650, color: 'var(--text)' }}>Mood</span> · {mood}
              </div>
            ) : null}

            {spec?.visualTags?.length ? (
              <div className="row" style={{ gap: 6, marginTop: 10, flexWrap: 'wrap' }}>
                {spec.visualTags.map((t) => (
                  <span
                    key={t}
                    style={{
                      border: '1px solid var(--border)',
                      background: 'rgba(127, 127, 140, 0.06)',
                      borderRadius: 999,
                      padding: '4px 10px',
                      fontSize: 11,
                      color: 'rgba(127, 127, 140, 0.98)',
                    }}
                  >
                    {t}
                  </span>
                ))}
              </div>
            ) : null}

            {meta && meta.length ? (
              <div className="row" style={{ gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
                {meta.map((m) => (
                  <span
                    key={m.label}
                    style={{
                      border: '1px solid var(--border)',
                      background: 'rgba(127, 127, 140, 0.06)',
                      borderRadius: 999,
                      padding: '6px 10px',
                      fontSize: 12,
                      color: 'rgba(127, 127, 140, 0.95)',
                    }}
                  >
                    <span style={{ color: 'var(--text)', fontWeight: 650 }}>{m.label}</span>
                    <span className="muted"> · {m.value}</span>
                  </span>
                ))}
              </div>
            ) : null}
          </div>
        </div>

        {spec?.visualSlots?.length ? (
          <div style={{ borderTop: '1px solid var(--border)', paddingTop: 10 }}>
            <div className="small muted" style={{ marginBottom: 8 }}>
              Visual accessories (six dimensions)
            </div>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))',
                gap: 8,
              }}
            >
              {spec.visualSlots.map((sl) => (
                <div
                  key={`${sl.slot}-${sl.dimension}`}
                  style={{
                    border: '1px solid var(--border)',
                    borderRadius: 12,
                    padding: '8px 10px',
                    background: 'rgba(127, 127, 140, 0.04)',
                  }}
                >
                  <div className="small muted" style={{ fontWeight: 650, color: 'var(--text)' }}>
                    {titleCaseSlot(sl.slot)}
                  </div>
                  <div className="small muted" style={{ marginTop: 2 }}>
                    {sl.dimension} · {sl.level}
                  </div>
                  <div style={{ fontSize: 12, marginTop: 4, lineHeight: 1.35 }}>{sl.descriptor}</div>
                </div>
              ))}
            </div>
          </div>
        ) : null}
      </div>
    </Card>
  )
}
