import { Navigate, Route, Routes } from 'react-router-dom'
import { WelcomePage } from '../pages/WelcomePage'
import { OnboardingPage } from '../pages/OnboardingPage'
import { UserProfileRevealPage } from '../pages/UserProfileRevealPage'
import { TripInputPage } from '../pages/TripInputPage'
import { TripProfileRevealPage } from '../pages/TripProfileRevealPage'
import { ResultPage } from '../pages/ResultPage'

export function Router() {
  return (
    <Routes>
      <Route path="/" element={<WelcomePage />} />
      <Route path="/onboarding" element={<OnboardingPage />} />
      <Route path="/profile/reveal" element={<UserProfileRevealPage />} />
      <Route path="/trip" element={<TripInputPage />} />
      <Route path="/trip/reveal" element={<TripProfileRevealPage />} />
      <Route path="/result" element={<ResultPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

