import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { RadarChart } from '../components/charts/RadarChart'
import { IdentityCard } from '../components/profile/IdentityCard'
import { refineTrip } from '../lib/api'
import { getCachedPortrait } from '../lib/identityPortraitCache'
import { session } from '../lib/session'
import type { DimensionKey, DimensionScores, TripResult } from '../types/contracts'
import { RefineOverlay } from '../components/refine/RefineOverlay'

function SectionTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="stack" style={{ gap: 6 }}>
      <div className="title h2">{title}</div>
      {subtitle ? <div className="muted">{subtitle}</div> : null}
    </div>
  )
}

function TripSummaryCard({ tripSummary }: { tripSummary: TripResult['tripSummary'] }) {
  const { origin, destination, durationText, budgetText, routeSummary, styleSummary } = tripSummary
  return (
    <Card>
      <div className="stack" style={{ gap: 12 }}>
        <SectionTitle title="Trip summary" />
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <div>
            <div className="small muted">From</div>
            <div style={{ fontWeight: 650 }}>{origin}</div>
          </div>
          <div>
            <div className="small muted">To</div>
            <div style={{ fontWeight: 650 }}>{destination}</div>
          </div>
          <div>
            <div className="small muted">Duration</div>
            <div style={{ fontWeight: 650 }}>{durationText}</div>
          </div>
          <div>
            <div className="small muted">Budget</div>
            <div style={{ fontWeight: 650 }}>{budgetText}</div>
          </div>
        </div>
        <div className="muted">{routeSummary}</div>
        <div style={{ padding: '10px 12px', borderRadius: 12, background: 'var(--accent-soft)' }}>
          <span className="small muted">Style</span>
          <div style={{ fontWeight: 600 }}>{styleSummary}</div>
        </div>
      </div>
    </Card>
  )
}

export function ResultPage() {
  const navigate = useNavigate()
  const [tripResult, setTripResult] = useState<TripResult | null>(() => session.getLastTripResult())
  const userProfile = useMemo(() => session.getUserProfile(), [])
  const [isRefineOpen, setIsRefineOpen] = useState(false)

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

  if (!userProfile) {
    return (
      <div className="container" style={{ paddingTop: 28 }}>
        <Card>
          <div className="stack">
            <div className="title h2">Missing profile</div>
            <div className="muted">
              This result page needs your baseline user profile. Please run onboarding again.
            </div>
            <div className="row">
              <Button onClick={() => navigate('/onboarding')}>Go to onboarding</Button>
              <Button variant="ghost" onClick={() => navigate('/trip')}>
                Back to trip input
              </Button>
            </div>
          </div>
        </Card>
      </div>
    )
  }

  const currentTripScores = tripResult.tripProfile.dimensionScores

  const changedAdjustmentsFromScores = (draft: DimensionScores): Partial<DimensionScores> => {
    return Object.fromEntries(
      (Object.keys(draft) as DimensionKey[])
        .filter((k) => draft[k] !== currentTripScores[k])
        .map((k) => [k, draft[k]])
    ) as Partial<DimensionScores>
  }

  return (
    <div className="container" style={{ paddingTop: 28 }}>
      <div className="stack" style={{ gap: 18 }}>
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <div>
            <div className="title h2">Your match</div>
            <div className="muted">You and this trip are expressed in the same dimension system.</div>
          </div>
          <div className="row">
            <Button variant="ghost" onClick={() => navigate('/trip')}>
              New request
            </Button>
          </div>
        </div>

        <Card>
          <div className="stack" style={{ gap: 14 }}>
            <SectionTitle title="Match level" subtitle="The fit score + overlap view, up front." />

            <div className="row" style={{ justifyContent: 'space-between', alignItems: 'stretch', gap: 12, flexWrap: 'wrap' }}>
              <div style={{ flex: '1 1 320px' }}>
                <div style={{ fontWeight: 750, fontSize: 34, letterSpacing: '-0.02em' }}>
                  {tripResult.fitAnalysis.overallFit.score}
                </div>
                <div className="muted" style={{ marginTop: 2 }}>
                  {tripResult.fitAnalysis.overallFit.label}
                </div>
                <div style={{ marginTop: 10 }}>{tripResult.fitAnalysis.summary}</div>

                <div className="row" style={{ gap: 18, alignItems: 'flex-start', marginTop: 14 }}>
                  <div style={{ flex: '1 1 220px' }}>
                    <div className="small muted">Top matches</div>
                    <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
                      {tripResult.fitAnalysis.topMatches.map((x) => (
                        <li key={x}>{x}</li>
                      ))}
                    </ul>
                  </div>
                  <div style={{ flex: '1 1 220px' }}>
                    <div className="small muted">Top mismatches</div>
                    <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
                      {tripResult.fitAnalysis.topMismatches.map((x) => (
                        <li key={x}>{x}</li>
                      ))}
                    </ul>
                  </div>
                </div>
              </div>

              <div style={{ flex: '1 1 360px' }}>
                <div className="row small muted" style={{ gap: 12, flexWrap: 'wrap' }}>
                  <span>
                    <span
                      aria-hidden="true"
                      style={{
                        display: 'inline-block',
                        width: 10,
                        height: 10,
                        borderRadius: 999,
                        background: 'var(--accent, #6e56cf)',
                        marginRight: 8,
                        verticalAlign: 'middle',
                      }}
                    />
                    You
                  </span>
                  <span>
                    <span
                      aria-hidden="true"
                      style={{
                        display: 'inline-block',
                        width: 10,
                        height: 10,
                        borderRadius: 999,
                        background: 'rgba(20, 184, 166, 0.95)',
                        marginRight: 8,
                        verticalAlign: 'middle',
                      }}
                    />
                    This trip
                  </span>
                </div>
                <div style={{ display: 'grid', placeItems: 'center', marginTop: 8 }}>
                  <RadarChart
                    scores={userProfile.dimensionScores}
                    compareScores={tripResult.tripProfile.dimensionScores}
                    size={360}
                    color="var(--accent, #6e56cf)"
                    compareColor="rgba(20, 184, 166, 0.95)"
                  />
                </div>
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="stack" style={{ gap: 14 }}>
            <SectionTitle title="User Profile" subtitle="Your traveler identity, description, and dimension profile." />

            <div className="row" style={{ gap: 12, alignItems: 'stretch', flexWrap: 'wrap' }}>
              <div style={{ flex: '1 1 420px' }}>
                <IdentityCard
                  title="Traveler identity"
                  spec={userProfile.travelerIdentitySpec ?? tripResult.travelerIdentitySpec}
                  portrait={userProfile.travelerPortrait}
                  fallbackName={userProfile.travelerArchetype.name}
                  fallbackDescription={userProfile.travelerArchetype.description}
                  meta={[
                    { label: 'Tone', value: userProfile.avatarConfig.styleTone },
                    { label: 'Energy', value: userProfile.avatarConfig.energyLevel },
                    { label: 'Vibe', value: userProfile.avatarConfig.vibe },
                  ]}
                />
              </div>
              <div style={{ flex: '1 1 340px' }}>
                <Card>
                  <div className="stack" style={{ gap: 12 }}>
                    <div className="small muted">User description</div>
                    <div style={{ fontWeight: 650, letterSpacing: '-0.01em' }}>{userProfile.profileSummary}</div>
                    <div style={{ borderTop: '1px solid var(--border)', paddingTop: 10 }}>
                      <div className="small muted">Dimension profile</div>
                      <div style={{ marginTop: 8, display: 'grid', placeItems: 'center' }}>
                        <RadarChart scores={userProfile.dimensionScores} size={260} color="var(--accent, #6e56cf)" />
                      </div>
                    </div>
                  </div>
                </Card>
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="stack" style={{ gap: 14 }}>
            <div className="row" style={{ justifyContent: 'space-between', alignItems: 'flex-end', gap: 12 }}>
              <SectionTitle
                title="Trip Profile"
                subtitle="This trip’s companion identity, description, and dimension profile."
              />
              <div className="stack" style={{ gap: 6, alignItems: 'flex-end' }}>
                <div className="small muted">Not happy with this trip?</div>
                <Button variant="ghost" onClick={() => setIsRefineOpen(true)}>
                  Refine
                </Button>
              </div>
            </div>

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
              <div style={{ flex: '1 1 340px' }}>
                <Card>
                  <div className="stack" style={{ gap: 12 }}>
                    <div className="small muted">Trip description</div>
                    <div style={{ fontWeight: 650, letterSpacing: '-0.01em' }}>{tripResult.tripSummary.styleSummary}</div>
                    <div className="muted">{tripResult.tripSummary.routeSummary}</div>
                    <div style={{ borderTop: '1px solid var(--border)', paddingTop: 10 }}>
                      <div className="small muted">Dimension profile</div>
                      <div style={{ marginTop: 8, display: 'grid', placeItems: 'center' }}>
                        <RadarChart
                          scores={tripResult.tripProfile.dimensionScores}
                          size={260}
                          color="rgba(20, 184, 166, 0.95)"
                        />
                      </div>
                    </div>
                  </div>
                </Card>
              </div>
            </div>
          </div>
        </Card>

        <TripSummaryCard tripSummary={tripResult.tripSummary} />

        <Card>
          <div className="stack" style={{ gap: 14 }}>
            <SectionTitle
              title="Extracted preferences"
              subtitle="The key requirements Taily understood from your prompt."
            />
            <div className="row" style={{ alignItems: 'flex-start' }}>
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Must-pass stops</div>
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
                <div className="small muted">Must-see / do</div>
                <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
                  {tripResult.extractedPreferences.mustSeeOrDo.map((x) => (
                    <li key={x}>{x}</li>
                  ))}
                </ul>
              </div>
            </div>
            <div className="row">
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Lodging preference</div>
                <div style={{ fontWeight: 600 }}>{tripResult.extractedPreferences.lodgingPreference}</div>
              </div>
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Budget preference</div>
                <div style={{ fontWeight: 600 }}>{tripResult.extractedPreferences.budgetPreference}</div>
              </div>
              <div style={{ flex: '1 1 260px' }}>
                <div className="small muted">Pace preference</div>
                <div style={{ fontWeight: 600 }}>{tripResult.extractedPreferences.pacePreference}</div>
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="stack">
            <SectionTitle title="Itinerary" subtitle="Structured by day." />
            <div className="stack" style={{ gap: 12 }}>
              {tripResult.itinerary.map((d) => (
                <div
                  key={d.day}
                  style={{
                    border: '1px solid var(--border)',
                    borderRadius: 'var(--radius-m)',
                    padding: 14,
                    background: 'rgba(127, 127, 140, 0.06)',
                  }}
                >
                  <div className="row" style={{ justifyContent: 'space-between' }}>
                    <div style={{ fontWeight: 700 }}>
                      Day {d.day}: {d.title}
                    </div>
                    <div className="small muted">{d.area}</div>
                  </div>
                  <div className="small muted" style={{ marginTop: 6 }}>
                    {d.vibeNote}
                  </div>
                  <ul style={{ margin: '10px 0 0', paddingLeft: 18 }}>
                    {d.activities.map((a) => (
                      <li key={a}>{a}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
        </Card>

        <Card>
          <div className="stack" style={{ gap: 14 }}>
            <SectionTitle title="Identity layer" subtitle="Structured traveler + companion packages (image-ready)." />
            <div className="row" style={{ alignItems: 'stretch', flexWrap: 'wrap', gap: 12 }}>
              <div style={{ flex: '1 1 320px' }}>
                <div className="small muted">Traveler archetype</div>
                <div style={{ fontWeight: 700, marginTop: 6 }}>{tripResult.travelerArchetype.name}</div>
                <div className="muted">{tripResult.travelerArchetype.description}</div>
                {tripResult.travelerIdentitySpec?.visualTags?.length ? (
                  <div className="row" style={{ gap: 6, marginTop: 8, flexWrap: 'wrap' }}>
                    {tripResult.travelerIdentitySpec.visualTags.map((t) => (
                      <span key={t} className="small muted" style={{ fontWeight: 650 }}>
                        {t}
                      </span>
                    ))}
                  </div>
                ) : null}
              </div>
              <div style={{ flex: '1 1 320px' }}>
                <div className="small muted">Trip companion-pet</div>
                <div style={{ fontWeight: 700, marginTop: 6 }}>{tripResult.tripCompanionPet.name}</div>
                <div className="muted">{tripResult.tripCompanionPet.description}</div>
                {tripResult.tripCompanionSpec?.visualTags?.length ? (
                  <div className="row" style={{ gap: 6, marginTop: 8, flexWrap: 'wrap' }}>
                    {tripResult.tripCompanionSpec.visualTags.map((t) => (
                      <span key={t} className="small muted" style={{ fontWeight: 650 }}>
                        {t}
                      </span>
                    ))}
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="stack" style={{ gap: 14 }}>
            <SectionTitle title="Worth considering" />
            <ul style={{ margin: 0, paddingLeft: 18 }}>
              {tripResult.worthConsidering.map((x) => (
                <li key={x.name}>
                  <span style={{ fontWeight: 650 }}>{x.name}</span>
                  <span className="muted"> — {x.reason}</span>
                </li>
              ))}
            </ul>
          </div>
        </Card>
      </div>

      <RefineOverlay
        open={isRefineOpen}
        onClose={() => setIsRefineOpen(false)}
        tripScores={tripResult.tripProfile.dimensionScores}
        tripStyleSummary={tripResult.tripSummary.styleSummary}
        userProfile={userProfile}
        initialCompanionSpecHash={tripResult.tripCompanionSpec?.specHash ?? ''}
        existingCompanionPortrait={tripResult.companionPortrait}
        onSubmit={async (draftScores, refinementText) =>
          refineTrip({
            userProfile: {
              dimensionScores: userProfile.dimensionScores,
              profileSummary: userProfile.profileSummary,
            },
            currentTrip: {
              tripSummary: tripResult.tripSummary,
              itinerary: tripResult.itinerary,
              tripProfile: { dimensionScores: tripResult.tripProfile.dimensionScores },
            },
            refinement: {
              dimensionAdjustments: changedAdjustmentsFromScores(draftScores),
              prompt: refinementText,
            },
          })
        }
        onTripFinalized={(updated) => setTripResult(updated)}
      />
    </div>
  )
}

