import { useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { TextArea } from '../components/ui/TextArea'
import { generateWithFit } from '../lib/api'
import { ensureCompanionPortrait } from '../lib/identityPortrait'
import { session } from '../lib/session'

function clampInt(n: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, Math.round(n)))
}

function budgetTextFromScore(score: number): { label: string; amount: number } {
  const s = clampInt(score, 1, 5)
  const mapping: Record<number, { label: string; amount: number }> = {
    1: { label: 'budget-friendly', amount: 1600 },
    2: { label: 'value-focused', amount: 2200 },
    3: { label: 'mid-range', amount: 3000 },
    4: { label: 'comfort-forward', amount: 4200 },
    5: { label: 'premium', amount: 6000 },
  }
  return mapping[s] ?? mapping[3]
}

function durationDaysFromProfile(paceScore: number): number {
  const s = clampInt(paceScore, 1, 5)
  // Lower pace score ≈ more relaxed ⇒ slightly longer feels better.
  const mapping: Record<number, number> = { 1: 6, 2: 5, 3: 4, 4: 4, 5: 3 }
  return mapping[s] ?? 4
}

function shortStyleLine(userProfile: ReturnType<typeof session.getUserProfile>): string {
  if (!userProfile) return 'a calm, balanced trip'
  const pace = userProfile.dimensionScores.pace
  const food = userProfile.dimensionScores.foodFocus
  const comfort = userProfile.dimensionScores.comfort
  const exploration = userProfile.dimensionScores.exploration

  const parts: string[] = []
  parts.push(pace <= 2 ? 'a relaxed pace' : pace >= 4 ? 'a fuller schedule' : 'a balanced pace')
  if (food >= 4) parts.push('food-forward days')
  if (comfort >= 4) parts.push('comfortable stays')
  if (exploration >= 4) parts.push('a few hidden gems')
  return parts.slice(0, 3).join(', ')
}

function buildDefaultPrompt(userProfile: ReturnType<typeof session.getUserProfile>): string {
  const days = durationDaysFromProfile(userProfile?.dimensionScores.pace ?? 3)
  const budget = budgetTextFromScore(userProfile?.dimensionScores.budget ?? 3)
  const style = shortStyleLine(userProfile)
  const destination = 'Tokyo'

  return `I want a ${days}-day trip to ${destination} with a ~$${budget.amount} budget. I prefer ${style}. Please include 1–2 memorable food experiences and keep transit simple.`
}

function buildSuggestionBubbles(userProfile: ReturnType<typeof session.getUserProfile>): string[] {
  const days = durationDaysFromProfile(userProfile?.dimensionScores.pace ?? 3)
  const budget = budgetTextFromScore(userProfile?.dimensionScores.budget ?? 3)
  const food = userProfile?.dimensionScores.foodFocus ?? 3
  const comfort = userProfile?.dimensionScores.comfort ?? 3
  const exploration = userProfile?.dimensionScores.exploration ?? 3

  const vibe =
    food >= 4 ? 'food trip' : exploration >= 4 ? 'hidden-gem trip' : comfort >= 4 ? 'comfortable trip' : 'city break'

  const longWeekend = Math.max(3, Math.min(4, days - 1))
  const beachDays = Math.max(4, Math.min(6, days + 1))

  const base = [
    `A ${days}-day Tokyo ${vibe} with a $${budget.amount} budget`,
    `A ${longWeekend}-day NYC weekend: museums + great food, $${Math.round(budget.amount * 0.65)} budget`,
    `A ${days}-day Lisbon trip with cozy cafes and a $${Math.round(budget.amount * 0.8)} budget`,
    `A ${days}-day Kyoto + Osaka trip with a $${Math.round(budget.amount * 0.95)} budget`,
    `A ${beachDays}-day Barcelona + coast trip with a $${Math.round(budget.amount * 1.05)} budget`,
  ]

  if (comfort >= 4) {
    base.unshift(`A ${days}-day Paris trip with comfortable stays and a $${Math.round(budget.amount * 1.1)} budget`)
  }
  if (exploration >= 4) {
    base.push(`A ${days}-day Mexico City trip with local favorites and a $${Math.round(budget.amount * 0.8)} budget`)
  }
  if (food >= 4) {
    base.push(`A ${days}-day Seoul food trip with a $${Math.round(budget.amount * 0.9)} budget`)
  }

  return Array.from(new Set(base)).slice(0, 12)
}

export function TripInputPage() {
  const navigate = useNavigate()
  const userProfile = useMemo(() => session.getUserProfile(), [])
  const suggestions = useMemo(() => buildSuggestionBubbles(userProfile), [userProfile])
  const defaultPrompt = useMemo(() => buildDefaultPrompt(userProfile), [userProfile])
  const [typed, setTyped] = useState('')
  const [isFocused, setIsFocused] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [loadingStep, setLoadingStep] = useState<0 | 1 | 2>(0)
  const [transition, setTransition] = useState<'idle' | 'trip' | 'companion' | 'reveal'>('idle')
  const [submitError, setSubmitError] = useState<string | null>(null)
  const timersRef = useRef<number[]>([])

  const showSmartDefault = !isFocused && typed.trim().length === 0
  const finalTyped = typed.trim()
  const canSubmit = (finalTyped.length > 0 || defaultPrompt.trim().length > 0) && !isLoading

  const planTrip = async (finalPrompt: string) => {
    if (isLoading) return
    setIsLoading(true)
    setLoadingStep(0)
    setTransition('trip')
    setSubmitError(null)

    // MVP scaffold: simulate "understanding → planning → matching fit"
    timersRef.current.forEach((t) => window.clearTimeout(t))
    timersRef.current = [
      window.setTimeout(() => setLoadingStep(1), 450),
      window.setTimeout(() => setLoadingStep(2), 900),
    ]

    try {
      const userProfile = session.getUserProfile()
      if (!userProfile) {
        throw new Error('Missing user profile. Please complete onboarding again.')
      }

      const prompt = finalPrompt.trim()
      const tripResult = await generateWithFit({
        userProfile: {
          dimensionScores: userProfile.dimensionScores,
          profileSummary: userProfile.profileSummary,
        },
        tripRequest: { prompt },
      })

      // Persist the core trip result immediately so reveal/result pages never land on "No trip yet"
      // if any subsequent image generation fails or localStorage is tight.
      session.setLastTripPrompt(prompt)
      session.setLastTripResult(tripResult)

      setTransition('companion')
      const withPortrait = await ensureCompanionPortrait(tripResult, userProfile)
      session.setLastTripResult(withPortrait)

      setTransition('reveal')
      navigate('/trip/reveal')
    } catch (e) {
      const message = e instanceof Error ? e.message : 'Trip generate failed'
      setSubmitError(message)
    } finally {
      timersRef.current.forEach((t) => window.clearTimeout(t))
      timersRef.current = []
      setTransition('idle')
      setIsLoading(false)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 28 }}>
      {transition !== 'idle' ? (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 40,
            background: 'rgba(19, 18, 26, 0.32)',
            backdropFilter: 'blur(6px)',
            WebkitBackdropFilter: 'blur(6px)',
            display: 'grid',
            placeItems: 'center',
            padding: 18,
          }}
        >
          <Card>
            <div className="stack" style={{ gap: 10, minWidth: 320, textAlign: 'center' }}>
              <div className="title h2" style={{ fontSize: 20 }}>
                {transition === 'trip'
                  ? 'Understanding your request…'
                  : transition === 'companion'
                    ? 'Creating your trip companion…'
                      : 'Preparing your reveal…'}
              </div>
              <div className="muted small">This usually takes a few seconds.</div>
              <div className="row" style={{ marginTop: 10, justifyContent: 'center', flexWrap: 'wrap' }}>
                <span className="bubble" style={{ position: 'static', opacity: 0.65 }}>
                  Understanding the trip
                </span>
                <span
                  className="bubble"
                  style={{ position: 'static', opacity: transition !== 'trip' ? 0.65 : 0.28 }}
                >
                  Generating the trip companion
                </span>
                <span
                  className="bubble"
                  style={{ position: 'static', opacity: transition === 'reveal' ? 0.65 : 0.28 }}
                >
                  Preparing the reveal
                </span>
              </div>
            </div>
          </Card>
        </div>
      ) : null}

      <div className="stack" style={{ maxWidth: 980, margin: '0 auto' }}>
        <div className="stack" style={{ gap: 8, textAlign: 'center', paddingTop: 26 }}>
          <div className="title h1">Where should we take you?</div>
          <div className="muted">Start from a smart suggestion, or type your own request.</div>
        </div>

        <div className="bubbleField" style={{ marginTop: 10 }}>
          {/* Ambient floating suggestions (soft + subtle) */}
          {[
            { text: suggestions[0], top: 12, left: 0, opacity: 0.3, delay: '0s' },
            { text: suggestions[1], top: 44, left: '22%', opacity: 0.22, delay: '1.2s' },
            { text: suggestions[2], top: 22, left: '64%', opacity: 0.24, delay: '0.6s' },
            { text: suggestions[3], top: 10, right: 0, opacity: 0.32, delay: '1.8s' },
            { text: suggestions[4], top: '44%', left: 0, opacity: 0.26, delay: '0.9s' },
            { text: suggestions[5], top: '54%', right: 0, opacity: 0.28, delay: '0.3s' },
            { text: suggestions[6], bottom: 48, right: '6%', opacity: 0.22, delay: '1.5s' },
            { text: suggestions[7], bottom: 22, right: 0, opacity: 0.2, delay: '0.75s' },
            { text: suggestions[8], bottom: 2, right: '14%', opacity: 0.18, delay: '1.1s' },
            { text: suggestions[9], bottom: 6, left: 0, opacity: 0.18, delay: '1.35s' },
            { text: suggestions[10], bottom: 62, left: '4%', opacity: 0.2, delay: '0.45s' },
            { text: suggestions[11], bottom: 44, left: '2%', opacity: 0.18, delay: '1.65s' },
          ].map((b) => (
            <button
              key={b.text}
              type="button"
              className="bubble"
              data-anim="1"
              style={{
                ...(typeof b.top !== 'undefined' ? { top: b.top } : null),
                ...(typeof b.bottom !== 'undefined' ? { bottom: b.bottom } : null),
                ...(typeof b.left !== 'undefined' ? { left: b.left } : null),
                ...(typeof b.right !== 'undefined' ? { right: b.right } : null),
                ['--bubbleOpacity' as never]: b.opacity,
                ['--bubbleDelay' as never]: b.delay,
              }}
              onClick={() => {
                void planTrip(b.text)
              }}
              aria-label={`Use suggestion: ${b.text}`}
            >
              {b.text}
            </button>
          ))}

          <div style={{ display: 'grid', placeItems: 'center', padding: '40px 0' }}>
            <div style={{ width: 'min(640px, 100%)' }}>
              <Card padding="var(--space-5)">
                <div className="stack">
                  <div className="smartPromptWrap">
                    {showSmartDefault ? (
                      <div className="smartPromptHint" aria-hidden="true">
                        {defaultPrompt}
                      </div>
                    ) : null}
                    <TextArea
                      value={typed}
                      onChange={(e) => setTyped(e.target.value)}
                      onFocus={() => setIsFocused(true)}
                      onBlur={() => setIsFocused(false)}
                      placeholder={showSmartDefault ? '' : 'Describe your trip…'}
                      style={{
                        background: 'transparent',
                        position: 'relative',
                        zIndex: 2,
                      }}
                    />
                  </div>

                  <Button
                    disabled={!canSubmit}
                    onClick={async () => {
                      const finalPrompt = finalTyped.length > 0 ? finalTyped : defaultPrompt
                      await planTrip(finalPrompt)
                    }}
                  >
                    Plan My Trip
                  </Button>

                  <div className="row" style={{ justifyContent: 'space-between' }}>
                    <Button variant="ghost" onClick={() => navigate('/')}>
                      Back
                    </Button>
                    <div />
                  </div>

                  {isLoading && (
                    <div style={{ marginTop: 4, borderTop: '1px solid var(--border)', paddingTop: 14 }}>
                      <div className="small muted">Working…</div>
                      <div className="row" style={{ marginTop: 10 }}>
                        <span className="bubble" style={{ position: 'static', opacity: loadingStep >= 0 ? 0.65 : 0.28 }}>
                          Understanding your travel style
                        </span>
                        <span className="bubble" style={{ position: 'static', opacity: loadingStep >= 1 ? 0.65 : 0.28 }}>
                          Planning your trip
                        </span>
                        <span className="bubble" style={{ position: 'static', opacity: loadingStep >= 2 ? 0.65 : 0.28 }}>
                          Matching this trip to you
                        </span>
                      </div>
                    </div>
                  )}

                  {submitError ? (
                    <div className="small" style={{ color: 'var(--danger, #b42318)' }}>
                      {submitError}
                    </div>
                  ) : null}
                </div>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

