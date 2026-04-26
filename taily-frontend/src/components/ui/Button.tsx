import type { ButtonHTMLAttributes, CSSProperties, ReactNode } from 'react'

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'ghost'
  children: ReactNode
}

export function Button({ variant = 'primary', children, ...props }: Props) {
  const base: CSSProperties = {
    borderRadius: 999,
    padding: '10px 14px',
    border: '1px solid var(--border)',
    cursor: 'pointer',
    fontWeight: 600,
    letterSpacing: '-0.01em',
    transition: 'transform 0.04s ease, box-shadow 0.2s ease, background 0.2s ease',
  }

  const styles: Record<NonNullable<Props['variant']>, CSSProperties> = {
    primary: {
      ...base,
      background: 'var(--accent)',
      color: 'var(--accent-ink)',
      borderColor: 'transparent',
      boxShadow: '0 10px 20px rgba(111, 92, 255, 0.25)',
    },
    ghost: {
      ...base,
      background: 'transparent',
      color: 'var(--text)',
    },
  }

  return (
    <button
      {...props}
      style={{
        ...styles[variant],
        opacity: props.disabled ? 0.6 : 1,
      }}
      onMouseDown={(e) => {
        props.onMouseDown?.(e)
        ;(e.currentTarget as HTMLButtonElement).style.transform = 'translateY(1px)'
      }}
      onMouseUp={(e) => {
        props.onMouseUp?.(e)
        ;(e.currentTarget as HTMLButtonElement).style.transform = 'translateY(0)'
      }}
    >
      {children}
    </button>
  )
}

