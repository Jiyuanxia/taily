import type { ReactNode } from 'react'

export function Card({
  children,
  padding = 'var(--space-5)',
}: {
  children: ReactNode
  padding?: string
}) {
  return (
    <div
      style={{
        background: 'var(--surface)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-l)',
        boxShadow: 'var(--shadow)',
        padding,
      }}
    >
      {children}
    </div>
  )
}

