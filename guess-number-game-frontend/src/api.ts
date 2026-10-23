import axios from 'axios'

export interface UserInfo {
  id: number
  username: string
  email: string
  score: number
  turns: number
}

export interface LoginResponse {
  userId: number
  username: string
  tokenType: string
  accessToken: string
}

export interface GameResult {
  won: boolean
  guessedNumber: number
  secretNumber: number
  remainingTurns: number
  currentScore: number
}

export interface LeaderboardEntry {
  username: string
  score: number
}

export interface TurnsPackage {
  packageId: string
  turns: number
  amount: number
  currency: string
}

export interface PaymentResult {
  status: string
  turnsAdded: number
  remainingTurns: number
  amount: number
  currency: string
}

export const api = axios.create({ baseURL: '/api' })

export function setAccessToken(token: string | null) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`
    localStorage.setItem('guess-number-token', token)
  } else {
    delete api.defaults.headers.common.Authorization
    localStorage.removeItem('guess-number-token')
  }
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data: unknown = error.response?.data
    if (typeof data === 'string' && data.trim()) return data
    if (data && typeof data === 'object' && 'message' in data) {
      return String(data.message)
    }
    if (!error.response) return 'Không kết nối được máy chủ. Hãy kiểm tra backend đang chạy.'
  }
  if (error instanceof Error && error.message) return error.message
  return 'Đã có lỗi xảy ra. Vui lòng thử lại.'
}
