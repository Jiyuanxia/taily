import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Chip } from '../components/ui/Chip'
import { Progress } from '../components/ui/Progress'
import { getMiddleOption, ONBOARDING_QUESTIONS } from '../data/onboarding'
import { createProfile } from '../lib/api'
import { ensureTravelerPortrait } from '../lib/identityPortrait'
import { session } from '../lib/session'
import type { DimensionScores, OnboardingAnswers } from '../types/contracts'

export function OnboardingPage() {
  const navigate = useNavigate()
  const defaults = useMemo(() => {
    const scores = {} as DimensionScores
    const answers = {} as OnboardingAnswers
    for (const q of ONBOARDING_QUESTIONS) {
      const mid = getMiddleOption(q)
      scores[q.key] = mid.score
      answers[q.key] = mid.value
    }
    return { scores, answers }
  }, [])

  const [stepIdx, setStepIdx] = useState(0)
  const [scores, setScores] = useState<DimensionScores>(defaults.scores)
  const [answers, setAnswers] = useState<OnboardingAnswers>(defaults.answers)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [transition, setTransition] = useState<'idle' | 'profile' | 'portrait'>('idle')

  const q = ONBOARDING_QUESTIONS[stepIdx]
  const total = ONBOARDING_QUESTIONS.length
  const currentStep = stepIdx + 1

  const selectedScore = useMemo(() => scores[q.key], [scores, q.key])

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
            <div className="stack" style={{ gap: 10, minWidth: 280, textAlign: 'center' }}>
              <div className="title h2" style={{ fontSize: 20 }}>
                {transition === 'profile' ? 'Saving your profile…' : 'Creating your traveler portrait…'}
              </div>
              <div className="muted small">This usually takes a few seconds.</div>
            </div>
          </Card>
        </div>
      ) : null}

      <div className="stack" style={{ maxWidth: 760 }}>
        <div className="stack" style={{ gap: 8 }}>
          <div className="title h2">Quick calibration</div>
          <div className="muted">
            Six short questions to set your baseline travel profile.
          </div>
        </div>

        <Card>
          <div className="stack">
            <Progress value={currentStep} max={total} />

            <div className="stack" style={{ gap: 8 }}>
              <div className="title h2">{q.title}</div>
              <div className="muted">{q.subtitle}</div>
            </div>

            <div className="row" style={{ gap: 10 }}>
              {q.options.map((opt) => (
                <Chip
                  key={opt.label}
                  selected={selectedScore === opt.score}
                  onClick={() => {
                    setScores((prev) => ({ ...prev, [q.key]: opt.score }))
                    setAnswers((prev) => ({ ...prev, [q.key]: opt.value }))
                  }}
                >
                  {opt.label}
                </Chip>
              ))}
            </div>

            <div className="row" style={{ justifyContent: 'space-between' }}>
              {stepIdx === 0 ? (
                <div />
              ) : (
                <Button
                  variant="ghost"
                  onClick={() => setStepIdx((i) => Math.max(0, i - 1))}
                >
                  Back
                </Button>
              )}

              {stepIdx < total - 1 ? (
                <Button onClick={() => setStepIdx((i) => Math.min(total - 1, i + 1))}>
                  Next
                </Button>
              ) : (
                <Button
                  disabled={isSubmitting}
                  onClick={async () => {
                    if (isSubmitting) return
                    setIsSubmitting(true)
                    setSubmitError(null)
                    try {
                      setTransition('profile')
                      const res = await createProfile({ onboardingAnswers: answers })
                      setTransition('portrait')
                      const withPortrait = await ensureTravelerPortrait(res.userProfile, null)
                      session.setUserProfile(withPortrait)
                      session.markOnboarded()
                      navigate('/profile/reveal')
                    } catch (e) {
                      const message = e instanceof Error ? e.message : 'Profile create failed'
                      setSubmitError(message)
                    } finally {
                      setTransition('idle')
                      setIsSubmitting(false)
                    }
                  }}
                >
                  {isSubmitting ? 'Finishing…' : 'Finish'}
                </Button>
              )}
            </div>

            {submitError ? (
              <div className="small" style={{ color: 'var(--danger, #b42318)' }}>
                {submitError}
              </div>
            ) : null}
          </div>
        </Card>
      </div>
    </div>
  )
}

