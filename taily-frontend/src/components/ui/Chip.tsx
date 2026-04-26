import type { ButtonHTMLAttributes, ReactNode } from 'react'

export function Chip({
  children,
  selected,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode
  selected?: boolean
}) {
  return (
    <button
      type="button"
      {...props}
      style={{
        borderRadius: 999,
        padding: '8px 12px',
        border: selected ? '1px solid transparent' : '1px solid var(--border)',
        background: selected ? 'var(--accent-soft)' : 'transparent',
        cursor: 'pointer',
        color: 'var(--text)',
      }}
    >
      {children}
    </button>
  )
}

