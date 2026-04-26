import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { RadarChart } from '../components/charts/RadarChart'
import { IdentityCard } from '../components/profile/IdentityCard'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { getCachedPortrait } from '../lib/identityPortraitCache'
import { session } from '../lib/session'
import type { TripResult } from '../types/contracts'

function SectionTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="stack" style={{ gap: 6 }}>
      <div className="title h2">{title}</div>
      {subtitle ? <div className="muted">{subtitle}</div> : null}
    </div>
  )
}

function TripSnapshot({ tripSummary }: { tripSummary: TripResult['tripSummary'] }) {
  const { origin, destination, durationText, budgetText, routeSummary, styleSummary } = tripSummary
  return (
    <Card>
      <div className="stack" style={{ gap: 12 }}>
        <div className="row" style={{ justifyContent: 'space-between', alignItems: 'flex-end', gap: 12 }}>
          <SectionTitle title="Your trip" subtitle="Here’s what Taily generated from your request." />
          <div className="row small muted" style={{ gap: 10 }}>
            <span>
              <span style={{ fontWeight: 650, color: 'var(--text)' }}>{origin}</span> →{' '}
              <span style={{ fontWeight: 650, color: 'var(--text)' }}>{destination}</span>
            </span>
            <span aria-hidden="true">·</span>
            <span>{durationText}</span>
            <span aria-hidden="true">·</span>
            <span>{budgetText}</span>
          </div>
        </div>

        <div style={{ padding: '10px 12px', borderRadius: 12, background: 'var(--accent-soft)' }}>
          <span className="small muted">Style</span>
          <div style={{ fontWeight: 650 }}>{styleSummary}</div>
        </div>
        <div className="muted">{routeSummary}</div>
      </div>
    </Card>
  )
}

export function TripProfileRevealPage() {
  const navigate = useNavigate()
  const tripResult = useMemo(() => session.getLastTripResult(), [])
  const lastPrompt = useMemo(() => session.getLastTripPrompt(), [])

  const cachedCompanionPortrait = useMemo(() => {
    const h = tripResult?.tripCompanionSpec?.specHash
    if (!h) return null
    const cached = getCachedPortrait(h)
    if (!cached) return null
    return { specHash: h, mimeType: cached.mimeType, base64: cached.base64 }
  }, [tripResult?.tripCompanionSpec?.specHash])

  if (!tripResult) {
    return (
      <div className="container" style={{ paddingTop: 28 }}>
        <Card>
          <div className="stack">
            <div className="title h2">No trip yet</div>
            <div className="muted">Start from the trip input page to generate a trip.</div>
            <div className="row">
              <Button onClick={() => navigate('/trip')}>Go to trip input</Button>
            </div>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="container" style={{ paddingTop: 28 }}>
      <div className="stack" style={{ gap: 18, maxWidth: 980, margin: '0 auto' }}>
        <div className="row" style={{ justifyContent: 'space-between', alignItems: 'flex-end', gap: 12 }}>
          <SectionTitle
            title="This trip’s profile"
            subtitle="Here’s the trip identity Taily shaped from your request."
          />
          <Button onClick={() => navigate('/result')}>Continue to full comparison</Button>
        </div>

        <TripSnapshot tripSummary={tripResult.tripSummary} />

        <Card>
          <div className="stack" style={{ gap: 12 }}>
            <SectionTitle title="What Taily picked up" subtitle="The key requirements extracted from your prompt." />
            <div className="row" style={{ alignItems: 'flex-start' }}>
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Must-pass</div>
                <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
                  {tripResult.extractedPreferences.mustPassStops.map((x) => (
                    <li key={x}>{x}</li>
                  ))}
                </ul>
              </div>
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Must-eat</div>
                <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
                  {tripResult.extractedPreferences.mustEat.map((x) => (
                    <li key={x}>{x}</li>
                  ))}
                </ul>
              </div>
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Must-do</div>
                <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
                  {tripResult.extractedPreferences.mustSeeOrDo.map((x) => (
                    <li key={x}>{x}</li>
                  ))}
                </ul>
              </div>
            </div>

            <div className="row small muted" style={{ gap: 10, flexWrap: 'wrap' }}>
              <span>
                <span style={{ fontWeight: 650, color: 'var(--text)' }}>Lodging</span> ·{' '}
                {tripResult.extractedPreferences.lodgingPreference}
              </span>
              <span aria-hidden="true">·</span>
              <span>
                <span style={{ fontWeight: 650, color: 'var(--text)' }}>Budget</span> ·{' '}
                {tripResult.extractedPreferences.budgetPreference}
              </span>
              <span aria-hidden="true">·</span>
              <span>
                <span style={{ fontWeight: 650, color: 'var(--text)' }}>Pace</span> ·{' '}
                {tripResult.extractedPreferences.pacePreference}
              </span>
            </div>

            {lastPrompt ? (
              <div className="small muted" style={{ borderTop: '1px solid var(--border)', paddingTop: 10 }}>
                Based on: “{lastPrompt.length > 140 ? `${lastPrompt.slice(0, 140).trim()}…` : lastPrompt}”
              </div>
            ) : null}
          </div>
        </Card>

        <div className="row" style={{ gap: 12, alignItems: 'stretch', flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 420px' }}>
            <IdentityCard
              title="Trip companion"
              spec={tripResult.tripCompanionSpec}
              portrait={tripResult.companionPortrait ?? cachedCompanionPortrait}
              fallbackName={tripResult.tripCompanionPet.name}
              fallbackDescription={tripResult.tripCompanionPet.description}
            />
          </div>

          <div style={{ flex: '1 1 360px' }}>
            <Card>
              <div className="stack" style={{ gap: 12 }}>
                <div className="small muted">Trip profile</div>
                <div style={{ fontWeight: 650, letterSpacing: '-0.01em' }}>{tripResult.tripSummary.styleSummary}</div>
                <div style={{ borderTop: '1px solid var(--border)', paddingTop: 10 }}>
                  <div className="small muted">Dimension profile</div>
                  <div style={{ marginTop: 8, display: 'grid', placeItems: 'center' }}>
                    <RadarChart
                      scores={tripResult.tripProfile.dimensionScores}
                      size={300}
                      color="rgba(20, 184, 166, 0.95)"
                    />
                  </div>
                </div>
                <div className="row" style={{ justifyContent: 'space-between' }}>
                  <Button variant="ghost" onClick={() => navigate('/trip')}>
                    New request
                  </Button>
                  <div />
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}

