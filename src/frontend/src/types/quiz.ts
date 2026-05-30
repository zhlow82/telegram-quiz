import type { Question } from './question'

export type QuizStatus = 'DRAFT' | 'ACTIVE' | 'STOPPED'

export interface QuizSummary {
  id: number
  name: string
  botUsername: string | null
  status: QuizStatus
  questionCount: number
  timePerQuestionSeconds: number
  passScorePercent: number
  createdAt: string
}

export interface Quiz {
  id: number
  name: string
  botTokenMasked: string
  botUsername: string | null
  timePerQuestionSeconds: number
  passScorePercent: number
  status: QuizStatus
  createdAt: string
  questions: Question[]
}

export interface QuizRequest {
  name: string
  botToken: string
  botUsername?: string
  timePerQuestionSeconds: number
  passScorePercent: number
  questionIds: number[]
}

export interface ValidateTokenRequest {
  token: string
}

export interface ValidateTokenResponse {
  valid: boolean
  botName: string | null
  username: string | null
}

export interface QuizSessionSummary {
  id: number
  telegramUserId: number
  telegramUsername: string | null
  telegramFirstName: string
  score: number
  totalQuestions: number
  passed: boolean
  status: 'IN_PROGRESS' | 'COMPLETED'
  startedAt: string
  completedAt: string | null
}
