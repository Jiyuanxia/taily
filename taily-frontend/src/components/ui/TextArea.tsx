import type { TextareaHTMLAttributes } from 'react'

export function TextArea(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      {...props}
      style={{
        width: '100%',
        minHeight: 170,
        resize: 'vertical',
        padding: '14px 14px',
        borderRadius: 'var(--radius-m)',
        border: '1px solid var(--border)',
        background: 'var(--surface)',
        color: 'var(--text)',
        lineHeight: 1.4,
        fontSize: 16,
      }}
    />
  )
}

