import { useMemo } from 'react'
import type { DimensionKey, DimensionScores } from '../../types/contracts'

const DIMENSION_ORDER: DimensionKey[] = [
  'budget',
  'pace',
  'foodFocus',
  'sightseeing',
  'comfort',
  'exploration',
]

const DIMENSION_LABEL: Record<DimensionKey, string> = {
  budget: 'Budget',
  pace: 'Pace',
  foodFocus: 'Food',
  sightseeing: 'Sightseeing',
  comfort: 'Comfort',
  exploration: 'Exploration',
}

function clamp01(x: number) {
  return Math.max(0, Math.min(1, x))
}

function pointsForScores({
  scores,
  cx,
  cy,
  r,
  minScore,
  maxScore,
}: {
  scores: DimensionScores
  cx: number
  cy: number
  r: number
  minScore: number
  maxScore: number
}): { x: number; y: number }[] {
  const range = Math.max(1, maxScore - minScore)
  const step = (Math.PI * 2) / DIMENSION_ORDER.length
  const start = -Math.PI / 2

  return DIMENSION_ORDER.map((k, idx) => {
    const t = clamp01((scores[k] - minScore) / range)
    const a = start + idx * step
    return {
      x: cx + Math.cos(a) * r * t,
      y: cy + Math.sin(a) * r * t,
    }
  })
}

function polygonPath(pts: { x: number; y: number }[]) {
  if (!pts.length) return ''
  return `M ${pts.map((p) => `${p.x.toFixed(2)} ${p.y.toFixed(2)}`).join(' L ')} Z`
}

export function RadarChart({
  scores,
  compareScores,
  size = 260,
  minScore = 1,
  maxScore = 5,
  color = 'var(--accent, #6e56cf)',
  compareColor = 'rgba(20, 184, 166, 0.95)',
  labelMode = 'minimal',
}: {
  scores: DimensionScores
  compareScores?: DimensionScores
  size?: number
  minScore?: 1
  maxScore?: 5
  color?: string
  compareColor?: string
  labelMode?: 'minimal' | 'none'
}) {
  const vb = useMemo(() => `0 0 ${size} ${size}`, [size])
  const cx = size / 2
  const cy = size / 2
  const padding = Math.max(22, Math.round(size * 0.14))
  const r = size / 2 - padding

  const gridRings = 4
  const gridColor = 'rgba(127, 127, 140, 0.28)'
  const axisColor = 'rgba(127, 127, 140, 0.35)'
  const labelColor = 'rgba(127, 127, 140, 0.9)'

  const primaryPts = useMemo(
    () => pointsForScores({ scores, cx, cy, r, minScore, maxScore }),
    [scores, cx, cy, r, minScore, maxScore]
  )
  const comparePts = useMemo(
    () =>
      compareScores ? pointsForScores({ scores: compareScores, cx, cy, r, minScore, maxScore }) : null,
    [compareScores, cx, cy, r, minScore, maxScore]
  )

  const step = (Math.PI * 2) / DIMENSION_ORDER.length
  const start = -Math.PI / 2

  return (
    <svg width={size} height={size} viewBox={vb} role="img" aria-label="Radar chart">
      {Array.from({ length: gridRings }, (_, i) => i + 1).map((ring) => {
        const rr = (r * ring) / gridRings
        const pts = DIMENSION_ORDER.map((_, idx) => {
          const a = start + idx * step
          return { x: cx + Math.cos(a) * rr, y: cy + Math.sin(a) * rr }
        })
        return (
          <path
            key={ring}
            d={polygonPath(pts)}
            fill="none"
            stroke={gridColor}
            strokeWidth={1}
            shapeRendering="geometricPrecision"
          />
        )
      })}

      {DIMENSION_ORDER.map((k, idx) => {
        const a = start + idx * step
        const x2 = cx + Math.cos(a) * r
        const y2 = cy + Math.sin(a) * r
        return (
          <line
            key={k}
            x1={cx}
            y1={cy}
            x2={x2}
            y2={y2}
            stroke={axisColor}
            strokeWidth={1}
            shapeRendering="geometricPrecision"
          />
        )
      })}

      <path
        d={polygonPath(primaryPts)}
        fill={color}
        fillOpacity={0.12}
        stroke={color}
        strokeOpacity={0.95}
        strokeWidth={2}
        shapeRendering="geometricPrecision"
      />

      {comparePts ? (
        <path
          d={polygonPath(comparePts)}
          fill={compareColor}
          fillOpacity={0.1}
          stroke={compareColor}
          strokeOpacity={0.92}
          strokeWidth={2}
          shapeRendering="geometricPrecision"
        />
      ) : null}

      {labelMode === 'minimal'
        ? DIMENSION_ORDER.map((k, idx) => {
            const a = start + idx * step
            const labelR = r + 12
            const x = cx + Math.cos(a) * labelR
            const y = cy + Math.sin(a) * labelR
            const textAnchor = Math.abs(Math.cos(a)) < 0.25 ? 'middle' : Math.cos(a) > 0 ? 'start' : 'end'
            const dominantBaseline =
              Math.abs(Math.sin(a)) < 0.25 ? 'middle' : Math.sin(a) > 0 ? 'hanging' : 'auto'
            return (
              <text
                key={k}
                x={x}
                y={y}
                fontSize={12}
                fill={labelColor}
                textAnchor={textAnchor}
                dominantBaseline={dominantBaseline as never}
                style={{ userSelect: 'none' }}
              >
                {DIMENSION_LABEL[k]}
              </text>
            )
          })
        : null}
    </svg>
  )
}

