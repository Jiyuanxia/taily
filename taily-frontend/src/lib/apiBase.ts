const DEFAULT_BASE_URL = 'http://localhost:8080'

export function getApiBaseUrl(): string {
  const fromEnv = (import.meta as unknown as { env?: Record<string, string> }).env?.VITE_API_BASE_URL
  return (fromEnv && fromEnv.trim().length > 0 ? fromEnv : DEFAULT_BASE_URL).replace(/\/+$/, '')
}
