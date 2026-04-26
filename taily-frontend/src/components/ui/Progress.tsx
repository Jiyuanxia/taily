export function Progress({
  value,
  max,
}: {
  value: number
  max: number
}) {
  const pct = max <= 0 ? 0 : Math.min(100, Math.max(0, (value / max) * 100))
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div
        style={{
          height: 10,
          background: 'rgba(127, 127, 140, 0.18)',
          borderRadius: 999,
          overflow: 'hidden',
          border: '1px solid var(--border)',
        }}
      >
        <div
          style={{
            height: '100%',
            width: `${pct}%`,
            background: 'var(--accent)',
            borderRadius: 999,
            transition: 'width 240ms ease',
          }}
        />
      </div>
      <div className="small muted">
        Step {value} of {max}
      </div>
    </div>
  )
}

