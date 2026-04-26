import { useEffect, useMemo, useRef, useState } from 'react'
import { EditableRadar } from '../charts/EditableRadar'
import { Button } from '../ui/Button'
import { Card } from '../ui/Card'
import { TextArea } from '../ui/TextArea'
import { maybeRefreshCompanionPortrait } from '../../lib/identityPortrait'
import { session } from '../../lib/session'
import type {
  DimensionKey,
  DimensionScores,
  IdentityPortrait,
  TripResult,
  UserProfile,
} from '../../types/contracts'

function sameScores(a: DimensionScores, b: DimensionScores): boolean {
  const keys: DimensionKey[] = ['budget', 'pace', 'foodFocus', 'sightseeing', 'comfort', 'exploration']
  return keys.every((k) => a[k] === b[k])
}

export function RefineOverlay({
  open,
  onClose,
  tripScores,
  tripStyleSummary,
  userProfile,
  initialCompanionSpecHash,
  existingCompanionPortrait,
  onSubmit,
  onTripFinalized,
}: {
  open: boolean
  onClose: () => void
  tripScores: DimensionScores
  tripStyleSummary: string
  userProfile: UserProfile
  initialCompanionSpecHash: string
  existingCompanionPortrait?: IdentityPortrait | null
  onSubmit: (draftScores: DimensionScores, refinementText: string) => Promise<TripResult>
  onTripFinalized?: (trip: TripResult) => void
}) {
  const [draftScores, setDraftScores] = useState<DimensionScores>(tripScores)
  const [text, setText] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [workPhase, setWorkPhase] = useState<'idle' | 'refine' | 'portrait'>('idle')
  const [error, setError] = useState<string | null>(null)
  const initialCompanionHashRef = useRef(initialCompanionSpecHash)

  const hasChanges = useMemo(
    () => !sameScores(draftScores, tripScores) || text.trim().length > 0,
    [draftScores, tripScores, text],
  )

  useEffect(() => {
    if (!open) return
    setDraftScores(tripScores)
    setText('')
    setError(null)
    setIsSubmitting(false)
    setWorkPhase('idle')
    initialCompanionHashRef.current = initialCompanionSpecHash
  }, [open, tripScores, initialCompanionSpecHash])

  useEffect(() => {
    if (!open) return
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape' && !isSubmitting) onClose()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [open, onClose, isSubmitting])

  if (!open) return null

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Refine trip"
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 50,
        background: 'rgba(19, 18, 26, 0.28)',
        backdropFilter: 'blur(6px)',
        WebkitBackdropFilter: 'blur(6px)',
        display: 'grid',
        placeItems: 'center',
        padding: 18,
      }}
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && !isSubmitting) onClose()
      }}
    >
      <div style={{ width: 'min(560px, 100%)', position: 'relative' }}>
        <Card padding="var(--space-5)">
          <div className="stack" style={{ gap: 12, opacity: workPhase === 'portrait' ? 0.45 : 1 }}>
            <div className="row" style={{ justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
              <div className="stack" style={{ gap: 6 }}>
                <div className="title h2">Refine this trip</div>
                <div className="muted">{tripStyleSummary}</div>
              </div>
              <Button variant="ghost" onClick={onClose} disabled={isSubmitting} aria-label="Close refine overlay">
                Close
              </Button>
            </div>

            <div style={{ borderTop: '1px solid var(--border)', paddingTop: 12 }}>
              <div className="small muted">Reshape the trip</div>
              <div className="muted small" style={{ marginTop: 6, lineHeight: 1.45 }}>
                Drag the handles. Values snap from 1–5.
              </div>
              <div style={{ display: 'grid', placeItems: 'center', marginTop: 10 }}>
                <EditableRadar scores={draftScores} onChange={setDraftScores} size={340} />
              </div>
            </div>

            <div style={{ borderTop: '1px solid var(--border)', paddingTop: 12 }}>
              <div className="small muted">What should change? (optional)</div>
              <div style={{ marginTop: 8 }}>
                <TextArea
                  value={text}
                  onChange={(e) => setText(e.target.value)}
                  placeholder="e.g., Slow the pace a bit, add more local food stops, and keep transit simple."
                />
              </div>
            </div>

            {error ? (
              <div className="small" style={{ color: 'var(--danger, #b42318)' }}>
                {error}
              </div>
            ) : null}

            <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
              <Button
                variant="ghost"
                onClick={() => {
                  setDraftScores(tripScores)
                  setText('')
                  setError(null)
                }}
                disabled={isSubmitting}
              >
                Reset
              </Button>

              <Button
                disabled={isSubmitting || !hasChanges}
                onClick={async () => {
                  if (isSubmitting) return
                  setIsSubmitting(true)
                  setWorkPhase('refine')
                  setError(null)
                  try {
                    const trip = await onSubmit(draftScores, text.trim())
                    const prevHash = initialCompanionHashRef.current
                    const nextHash = trip.tripCompanionSpec?.specHash
                    if (nextHash && nextHash !== prevHash) {
                      setWorkPhase('portrait')
                    }
                    const finalTrip = await maybeRefreshCompanionPortrait(prevHash, trip, userProfile, {
                      previousPortrait: existingCompanionPortrait ?? null,
                    })
                    session.setLastTripResult(finalTrip)
                    onTripFinalized?.(finalTrip)
                    onClose()
                  } catch (e) {
                    const message = e instanceof Error ? e.message : 'Trip refine failed'
                    setError(message)
                  } finally {
                    setWorkPhase('idle')
                    setIsSubmitting(false)
                  }
                }}
              >
                {isSubmitting
                  ? workPhase === 'portrait'
                    ? 'Updating portrait…'
                    : 'Refining…'
                  : 'Regenerate'}
              </Button>
            </div>
          </div>

          {workPhase === 'portrait' ? (
            <div
              style={{
                position: 'absolute',
                inset: 0,
                display: 'grid',
                placeItems: 'center',
                pointerEvents: 'none',
              }}
            >
              <div
                className="small muted"
                style={{
                  background: 'var(--surface)',
                  border: '1px solid var(--border)',
                  borderRadius: 12,
                  padding: '12px 16px',
                  fontWeight: 650,
                  color: 'var(--text)',
                  boxShadow: '0 8px 28px rgba(0,0,0,0.08)',
                }}
              >
                Updating companion portrait…
              </div>
            </div>
          ) : null}
        </Card>
      </div>
    </div>
  )
}
