import { useEffect, useMemo, useRef, useState } from 'react'
import type { DimensionKey, DimensionScores } from '../../types/contracts'

const DIMENSIONS: { key: DimensionKey; label: string }[] = [
  { key: 'budget', label: 'Budget' },
  { key: 'pace', label: 'Pace' },
  { key: 'foodFocus', label: 'Food' },
  { key: 'sightseeing', label: 'Sightseeing' },
  { key: 'comfort', label: 'Comfort' },
  { key: 'exploration', label: 'Exploration' },
]

function clamp(n: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, n))
}

function snapTo15(v: number): 1 | 2 | 3 | 4 | 5 {
  const s = Math.round(clamp(v, 1, 5))
  return (s === 1 ? 1 : s === 2 ? 2 : s === 3 ? 3 : s === 4 ? 4 : 5) as 1 | 2 | 3 | 4 | 5
}

export function EditableRadar({
  scores,
  onChange,
  size = 320,
}: {
  scores: DimensionScores
  onChange: (next: DimensionScores) => void
  size?: number
}) {
  const svgRef = useRef<SVGSVGElement | null>(null)
  const scoresRef = useRef(scores)
  const onChangeRef = useRef(onChange)
  const [dragKey, setDragKey] = useState<DimensionKey | null>(null)
  const dragKeyRef = useRef<DimensionKey | null>(null)
  const capturedPointerId = useRef<number | null>(null)

  useEffect(() => {
    scoresRef.current = scores
  }, [scores])

  useEffect(() => {
    onChangeRef.current = onChange
  }, [onChange])

  const center = size / 2
  const padding = Math.max(22, Math.round(size * 0.09))
  const radius = center - padding

  const angles = useMemo(() => {
    const step = (Math.PI * 2) / DIMENSIONS.length
    return DIMENSIONS.map((d, i) => ({ key: d.key, label: d.label, angle: -Math.PI / 2 + i * step }))
  }, [])

  const rings = [1, 2, 3, 4, 5]

  function valueToR(v: number): number {
    return (clamp(v, 1, 5) / 5) * radius
  }

  function pointFor(key: DimensionKey, value: number): { x: number; y: number } {
    const a = angles.find((x) => x.key === key)?.angle ?? 0
    const r = valueToR(value)
    return {
      x: center + Math.cos(a) * r,
      y: center + Math.sin(a) * r,
    }
  }

  const polygon = useMemo(() => {
    return DIMENSIONS.map((d) => {
      const p = pointFor(d.key, scores[d.key])
      return `${p.x},${p.y}`
    }).join(' ')
  }, [scores, size, angles, center, radius])

  function svgPointFromClient(clientX: number, clientY: number): { x: number; y: number } | null {
    const svg = svgRef.current
    if (!svg) return null
    const rect = svg.getBoundingClientRect()
    const x = clientX - rect.left
    const y = clientY - rect.top
    return { x, y }
  }

  function updateKeyFromClient(key: DimensionKey, clientX: number, clientY: number) {
    const p = svgPointFromClient(clientX, clientY)
    if (!p) return

    const a = angles.find((x) => x.key === key)?.angle ?? 0
    const ux = Math.cos(a)
    const uy = Math.sin(a)

    const dx = p.x - center
    const dy = p.y - center
    const proj = dx * ux + dy * uy

    const raw = (proj / radius) * 5
    const snapped = snapTo15(raw)

    const current = scoresRef.current
    if (snapped !== current[key]) {
      const next = { ...current, [key]: snapped }
      scoresRef.current = next
      onChangeRef.current(next)
    }
  }

  function endDrag() {
    const svg = svgRef.current
    const pid = capturedPointerId.current
    if (svg && pid != null) {
      try {
        svg.releasePointerCapture(pid)
      } catch {
        // ignore if already released
      }
    }
    capturedPointerId.current = null
    dragKeyRef.current = null
    setDragKey(null)
  }

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (!dragKeyRef.current) return
      if (e.key === 'Escape') endDrag()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [])

  useEffect(() => {
    if (!dragKey) return

    function onMove(e: PointerEvent) {
      const k = dragKeyRef.current
      if (!k) return
      updateKeyFromClient(k, e.clientX, e.clientY)
    }

    function onUp(e: PointerEvent) {
      if (capturedPointerId.current != null && e.pointerId !== capturedPointerId.current) return
      endDrag()
    }

    window.addEventListener('pointermove', onMove, { capture: true })
    window.addEventListener('pointerup', onUp, { capture: true })
    window.addEventListener('pointercancel', onUp, { capture: true })
    return () => {
      window.removeEventListener('pointermove', onMove, { capture: true })
      window.removeEventListener('pointerup', onUp, { capture: true })
      window.removeEventListener('pointercancel', onUp, { capture: true })
    }
  }, [dragKey, angles, center, radius])

  return (
    <svg
      ref={svgRef}
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
      role="img"
      aria-label="Editable trip profile radar"
      style={{
        touchAction: 'none',
        display: 'block',
        maxWidth: '100%',
      }}
      onPointerUp={(e) => {
        if (capturedPointerId.current != null && e.pointerId === capturedPointerId.current) endDrag()
      }}
      onPointerCancel={() => endDrag()}
    >
      {rings.map((r) => {
        const rr = valueToR(r)
        return (
          <circle
            key={r}
            cx={center}
            cy={center}
            r={rr}
            fill="none"
            stroke="rgba(127, 127, 140, 0.18)"
            strokeWidth={1}
          />
        )
      })}

      {angles.map((d) => {
        const x2 = center + Math.cos(d.angle) * radius
        const y2 = center + Math.sin(d.angle) * radius

        const lx = center + Math.cos(d.angle) * (radius + 14)
        const ly = center + Math.sin(d.angle) * (radius + 14)

        const anchor =
          Math.abs(Math.cos(d.angle)) < 0.2 ? 'middle' : Math.cos(d.angle) > 0 ? 'start' : 'end'

        return (
          <g key={d.key}>
            <line
              x1={center}
              y1={center}
              x2={x2}
              y2={y2}
              stroke="rgba(127, 127, 140, 0.20)"
              strokeWidth={1}
            />
            <text
              x={lx}
              y={ly}
              textAnchor={anchor}
              dominantBaseline="middle"
              style={{
                fontSize: 12,
                fill: 'rgba(127, 127, 140, 0.95)',
                userSelect: 'none',
              }}
            >
              {d.label}
            </text>
          </g>
        )
      })}

      <polygon points={polygon} fill="rgba(20, 184, 166, 0.18)" stroke="rgba(20, 184, 166, 0.95)" strokeWidth={2} />

      {DIMENSIONS.map((d) => {
        const p = pointFor(d.key, scores[d.key])
        return (
          <g key={d.key}>
            <circle
              cx={p.x}
              cy={p.y}
              r={16}
              fill="transparent"
              style={{ cursor: 'grab' }}
              onPointerDown={(e) => {
                e.preventDefault()
                e.stopPropagation()
                const svg = svgRef.current
                if (!svg) return
                try {
                  svg.setPointerCapture(e.pointerId)
                  capturedPointerId.current = e.pointerId
                } catch {
                  capturedPointerId.current = null
                }
                dragKeyRef.current = d.key
                setDragKey(d.key)
                updateKeyFromClient(d.key, e.clientX, e.clientY)
              }}
            />
            <circle
              cx={p.x}
              cy={p.y}
              r={7}
              fill="var(--surface)"
              stroke="rgba(20, 184, 166, 0.95)"
              strokeWidth={2}
              style={{ pointerEvents: 'none' }}
            />
          </g>
        )
      })}
    </svg>
  )
}
