import type { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'

export function AppShell({ children }: { children: ReactNode }) {
  const location = useLocation()
  const path = location.pathname

  const navItems: { to: string; label: string; when?: (p: string) => boolean }[] = [
    { to: '/trip', label: 'Plan' },
    { to: '/profile/reveal', label: 'Profile Reveal' },
  ]

  return (
    <div style={{ minHeight: '100svh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ padding: '22px 0' }}>
        <div className="container row" style={{ justifyContent: 'space-between', gap: 12 }}>
          <Link
            to="/"
            aria-label="Home"
            className="row"
            style={{
              gap: 12,
              padding: '8px 10px',
              marginLeft: -10,
              borderRadius: 14,
              border: '1px solid transparent',
              transition: 'background 160ms ease, border-color 160ms ease, transform 40ms ease',
            }}
            onMouseDown={(e) => {
              ;(e.currentTarget as HTMLAnchorElement).style.transform = 'translateY(1px)'
            }}
            onMouseUp={(e) => {
              ;(e.currentTarget as HTMLAnchorElement).style.transform = 'translateY(0)'
            }}
            onMouseEnter={(e) => {
              ;(e.currentTarget as HTMLAnchorElement).style.background = 'rgba(127, 127, 140, 0.06)'
              ;(e.currentTarget as HTMLAnchorElement).style.borderColor = 'var(--border)'
            }}
            onMouseLeave={(e) => {
              ;(e.currentTarget as HTMLAnchorElement).style.background = 'transparent'
              ;(e.currentTarget as HTMLAnchorElement).style.borderColor = 'transparent'
              ;(e.currentTarget as HTMLAnchorElement).style.transform = 'translateY(0)'
            }}
          >
            <img
              aria-hidden="true"
              src="/taily-icon.png"
              alt=""
              style={{
                width: 26,
                height: 26,
                borderRadius: 10,
                border: '1px solid var(--border)',
                objectFit: 'cover',
                display: 'block',
              }}
            />
            <div>
              <div style={{ fontWeight: 650, letterSpacing: '-0.01em' }}>Taily</div>
              <div className="small muted">Plan trips that fit you.</div>
            </div>
          </Link>

          <nav aria-label="Primary" className="row" style={{ gap: 8, justifyContent: 'flex-end' }}>
            {navItems
              .filter((x) => (x.when ? x.when(path) : true))
              .map((item) => {
                const active = path === item.to
                return (
                  <Link
                    key={item.to}
                    to={item.to}
                    aria-current={active ? 'page' : undefined}
                    style={{
                      padding: '8px 12px',
                      borderRadius: 999,
                      border: '1px solid var(--border)',
                      background: active ? 'var(--surface)' : 'rgba(127, 127, 140, 0.06)',
                      boxShadow: active ? 'var(--shadow)' : 'none',
                      fontSize: 13,
                      fontWeight: 650,
                      letterSpacing: '-0.01em',
                      color: 'var(--text)',
                      transition: 'background 160ms ease, box-shadow 160ms ease',
                    }}
                  >
                    {item.label}
                  </Link>
                )
              })}
          </nav>
        </div>
      </header>

      <main style={{ flex: 1, paddingBottom: 56 }}>{children}</main>

      <footer style={{ padding: '20px 0', borderTop: '1px solid var(--border)' }}>
        <div className="container small muted">
          Taily MVP (local session simulation)
        </div>
      </footer>
    </div>
  )
}

