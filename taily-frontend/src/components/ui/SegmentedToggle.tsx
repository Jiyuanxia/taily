import type { ReactNode } from 'react'

export function SegmentedToggle<T extends string>({
  value,
  onChange,
  options,
}: {
  value: T
  onChange: (value: T) => void
  options: { value: T; label: ReactNode }[]
}) {
  return (
    <div
      role="group"
      aria-label="Toggle"
      style={{
        display: 'inline-flex',
        gap: 4,
        padding: 4,
        borderRadius: 999,
        border: '1px solid var(--border)',
        background: 'rgba(127, 127, 140, 0.06)',
      }}
    >
      {options.map((opt) => {
        const selected = opt.value === value
        return (
          <button
            key={opt.value}
            type="button"
            onClick={() => onChange(opt.value)}
            aria-pressed={selected}
            style={{
              border: 'none',
              cursor: 'pointer',
              borderRadius: 999,
              padding: '8px 12px',
              fontSize: 13,
              fontWeight: 650,
              letterSpacing: '-0.01em',
              color: selected ? 'var(--text)' : 'rgba(127, 127, 140, 0.95)',
              background: selected ? 'var(--surface)' : 'transparent',
              boxShadow: selected ? 'var(--shadow)' : 'none',
            }}
          >
            {opt.label}
          </button>
        )
      })}
    </div>
  )
}

