import type { DimensionKey, DimensionScores } from '../types/contracts'

export type OnboardingOption = {
  label: string
  score: DimensionScores[DimensionKey]
  value: string
}

export type OnboardingQuestion = {
  key: DimensionKey
  title: string
  subtitle: string
  options: OnboardingOption[]
}

export const ONBOARDING_QUESTIONS: OnboardingQuestion[] = [
  {
    key: 'budget',
    title: 'How do you like to spend on a trip?',
    subtitle: 'A quick calibration — no wrong answers.',
    options: [
      { label: 'Keep it low', score: 1, value: 'low' },
      { label: 'Balanced', score: 3, value: 'balanced' },
      { label: 'Treat myself', score: 5, value: 'high' },
    ],
  },
  {
    key: 'pace',
    title: 'What pace feels best?',
    subtitle: 'How full should your days feel?',
    options: [
      { label: 'Relaxed', score: 1, value: 'relaxed' },
      { label: 'A mix', score: 3, value: 'balanced' },
      { label: 'Packed', score: 5, value: 'high' },
    ],
  },
  {
    key: 'foodFocus',
    title: 'How central is food to your trip?',
    subtitle: 'Restaurants, markets, and everything in between.',
    options: [
      { label: 'Nice to have', score: 2, value: 'low-medium' },
      { label: 'Pretty important', score: 4, value: 'medium-high' },
      { label: 'The main event', score: 5, value: 'high' },
    ],
  },
  {
    key: 'sightseeing',
    title: 'How much classic sightseeing?',
    subtitle: 'Landmarks and must-sees.',
    options: [
      { label: 'A little', score: 2, value: 'low-medium' },
      { label: 'Some', score: 3, value: 'medium' },
      { label: 'A lot', score: 5, value: 'high' },
    ],
  },
  {
    key: 'comfort',
    title: 'How much do you prioritize comfort?',
    subtitle: 'Lodging, transit ease, and downtime.',
    options: [
      { label: 'I’m flexible', score: 2, value: 'low-medium' },
      { label: 'I like balance', score: 4, value: 'medium-high' },
      { label: 'Very important', score: 5, value: 'high' },
    ],
  },
  {
    key: 'exploration',
    title: 'How much exploration do you want?',
    subtitle: 'Hidden gems and wandering vs. the hits.',
    options: [
      { label: 'Mostly familiar', score: 2, value: 'low-medium' },
      { label: 'A mix', score: 3, value: 'medium' },
      { label: 'A lot of discovery', score: 5, value: 'high' },
    ],
  },
]

export const DEFAULT_DIMENSION_SCORES: DimensionScores = {
  budget: 3,
  pace: 3,
  foodFocus: 3,
  sightseeing: 3,
  comfort: 3,
  exploration: 3,
}

export function getMiddleOption(q: OnboardingQuestion): OnboardingOption {
  return q.options[Math.floor(q.options.length / 2)]
}

