import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { RadarChart } from '../components/charts/RadarChart'
import { IdentityCard } from '../components/profile/IdentityCard'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { session } from '../lib/session'

function SectionTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="stack" style={{ gap: 6 }}>
      <div className="title h2">{title}</div>
      {subtitle ? <div className="muted">{subtitle}</div> : null}
    </div>
  )
}

export function UserProfileRevealPage() {
  const navigate = useNavigate()
  const userProfile = useMemo(() => session.getUserProfile(), [])

  if (!userProfile) {
    return (
      <div className="container" style={{ paddingTop: 28 }}>
        <Card>
          <div className="stack">
            <div className="title h2">No profile yet</div>
            <div className="muted">Complete onboarding to generate your baseline travel profile.</div>
            <div className="row">
              <Button onClick={() => navigate('/onboarding')}>Go to onboarding</Button>
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
            title="Your traveler profile"
            subtitle="Here’s what Taily learned about how you like to travel."
          />
          <Button onClick={() => navigate('/trip')}>Continue to trip planning</Button>
        </div>

        <div className="row" style={{ gap: 12, alignItems: 'stretch', flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 420px' }}>
            <IdentityCard
              title="Traveler identity"
              spec={userProfile.travelerIdentitySpec}
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

          <div style={{ flex: '1 1 360px' }}>
            <Card>
              <div className="stack" style={{ gap: 12 }}>
                <div className="small muted">Short description</div>
                <div style={{ fontWeight: 650, letterSpacing: '-0.01em' }}>{userProfile.profileSummary}</div>
                <div style={{ borderTop: '1px solid var(--border)', paddingTop: 10 }}>
                  <div className="small muted">Dimension profile</div>
                  <div style={{ marginTop: 8, display: 'grid', placeItems: 'center' }}>
                    <RadarChart scores={userProfile.dimensionScores} size={300} color="var(--accent, #6e56cf)" />
                  </div>
                </div>
                <div className="row" style={{ justifyContent: 'space-between' }}>
                  <Button variant="ghost" onClick={() => navigate('/onboarding')}>
                    Back
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

