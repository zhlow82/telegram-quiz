import api from './api'
import type { Quiz, QuizSummary, QuizRequest, ValidateTokenRequest, ValidateTokenResponse, QuizSessionSummary } from '@/types/quiz'

export const quizService = {
  list: (): Promise<QuizSummary[]> =>
    api.get<QuizSummary[]>('/api/quizzes').then(r => r.data),

  get: (id: number): Promise<Quiz> =>
    api.get<Quiz>(`/api/quizzes/${id}`).then(r => r.data),

  create: (data: QuizRequest): Promise<Quiz> =>
    api.post<Quiz>('/api/quizzes', data).then(r => r.data),

  update: (id: number, data: QuizRequest): Promise<Quiz> =>
    api.put<Quiz>(`/api/quizzes/${id}`, data).then(r => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/api/quizzes/${id}`).then(() => undefined),

  activate: (id: number): Promise<Quiz> =>
    api.post<Quiz>(`/api/quizzes/${id}/activate`).then(r => r.data),

  stop: (id: number): Promise<Quiz> =>
    api.post<Quiz>(`/api/quizzes/${id}/stop`).then(r => r.data),

  validateToken: (req: ValidateTokenRequest): Promise<ValidateTokenResponse> =>
    api.post<ValidateTokenResponse>('/api/bot/validate-token', req).then(r => r.data),

  getSessions: (id: number): Promise<QuizSessionSummary[]> =>
    api.get<QuizSessionSummary[]>(`/api/quizzes/${id}/sessions`).then(r => r.data),
}
