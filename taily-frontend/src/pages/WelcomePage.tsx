import { useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'
import { session } from '../lib/session'

export function WelcomePage() {
  const navigate = useNavigate()
  const returning = useMemo(() => session.isReturningUser(), [])

  useEffect(() => {
    if (!('geolocation' in navigator)) return

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        session.setHomeLocation({
          lat: pos.coords.latitude,
          lon: pos.coords.longitude,
          accuracyMeters: typeof pos.coords.accuracy === 'number' ? pos.coords.accuracy : undefined,
          capturedAt: Date.now(),
        })
      },
      () => {
        // Fail silently (permission denied / unavailable / timeout)
      },
      {
        enableHighAccuracy: false,
        timeout: 6000,
        maximumAge: 10 * 60 * 1000,
      }
    )
  }, [])

  return (
    <div className="container" style={{ paddingTop: 28 }}>
      <div style={{ display: 'grid', placeItems: 'center', padding: '34px 0 10px' }}>
        <div className="stack" style={{ width: 'min(760px, 100%)', textAlign: 'center', gap: 12 }}>
          <div className="title h1">Plan trips that fit you.</div>
          <div className="muted" style={{ fontSize: 16, lineHeight: 1.55 }}>
            Taily helps you plan an itinerary and understand how well it matches your travel style —
            then refine until it feels right.
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', placeItems: 'center', padding: '10px 0 34px' }}>
        <div style={{ width: 'min(640px, 100%)' }}>
          <div className="stack" style={{ gap: 12 }}>
            <button
              type="button"
              onClick={() => navigate(returning ? '/trip' : '/onboarding')}
              aria-label={returning ? 'Continue to trip input' : 'Continue to onboarding'}
              style={{
                width: '100%',
                textAlign: 'left',
                background: 'var(--surface)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-l)',
                boxShadow: 'var(--shadow)',
                padding: 'var(--space-5)',
                cursor: 'pointer',
                transition: 'transform 0.06s ease, box-shadow 0.2s ease, border-color 0.2s ease',
              }}
              onMouseDown={(e) => {
                ;(e.currentTarget as HTMLButtonElement).style.transform = 'translateY(1px)'
              }}
              onMouseUp={(e) => {
                ;(e.currentTarget as HTMLButtonElement).style.transform = 'translateY(0)'
              }}
            >
              <div className="stack" style={{ gap: 10 }}>
                <div className="row" style={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div className="stack" style={{ gap: 6 }}>
                    <div className="title h2">Start planning</div>
                    <div className="muted">
                      {returning
                        ? 'Welcome back — we’ll skip onboarding.'
                        : 'First time here? We’ll do a quick calibration.'}
                    </div>
                  </div>
                </div>

                <div className="small muted">Local session simulation only.</div>
              </div>
            </button>

            <div className="row" style={{ justifyContent: 'flex-end' }}>
              <Button
                variant="ghost"
                onClick={() => {
                  session.clear()
                  navigate('/onboarding')
                }}
              >
                Reset (simulate first-time)
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

