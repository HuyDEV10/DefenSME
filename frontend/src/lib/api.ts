import type { Asset, AssetPage, AssetQuery, AssetRequest } from '../features/assets/types'

const API_URL = (import.meta.env.VITE_API_URL || '/api/v1').replace(/\/$/, '')
const SESSION_KEY = 'defensme.session'

export interface Session {
  accessToken: string
  refreshToken: string
  accessTokenExpiresAt: string
  refreshTokenExpiresAt: string
  user: { id: string; organizationId: string; email: string; displayName: string; roles: string[] }
}
interface ProblemResponse { detail?: string; code?: string }
export class ApiError extends Error { constructor(message: string, readonly status: number, readonly code?: string) { super(message) } }
function token() { return loadSession()?.accessToken }

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const accessToken = token()
  const response = await fetch(`${API_URL}${path}`, { ...options, headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}), ...options.headers } })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({})) as ProblemResponse
    throw new ApiError(problem.detail || 'Yêu cầu không thể hoàn tất.', response.status, problem.code)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  login: (body: { organizationId: string; email: string; password: string }) => request<Session>('/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  assets: (query: AssetQuery, signal?: AbortSignal) => { const params = new URLSearchParams(); Object.entries(query).forEach(([key, value]) => { if (value !== '' && value !== undefined) params.set(key, String(value)) }); return request<AssetPage>(`/assets?${params}`, { signal }) },
  asset: (id: string) => request<Asset>(`/assets/${id}`),
  createAsset: (body: AssetRequest) => request<Asset>('/assets', { method: 'POST', body: JSON.stringify(body) }),
  updateAsset: (id: string, body: AssetRequest) => request<Asset>(`/assets/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  archiveAsset: (id: string) => request<void>(`/assets/${id}/archive`, { method: 'POST' }),
}

export function loadSession(): Session | null { try { const value = sessionStorage.getItem(SESSION_KEY); return value ? JSON.parse(value) as Session : null } catch { return null } }
export function saveSession(session: Session) { sessionStorage.setItem(SESSION_KEY, JSON.stringify(session)) }
export function clearSession() { sessionStorage.removeItem(SESSION_KEY) }
