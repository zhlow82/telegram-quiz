import api from './api'
import type { Question, QuestionRequest } from '@/types/question'

export const questionsService = {
  list: (): Promise<Question[]> =>
    api.get<Question[]>('/api/questions').then(r => r.data),

  get: (id: number): Promise<Question> =>
    api.get<Question>(`/api/questions/${id}`).then(r => r.data),

  create: (data: QuestionRequest): Promise<Question> =>
    api.post<Question>('/api/questions', data).then(r => r.data),

  update: (id: number, data: QuestionRequest): Promise<Question> =>
    api.put<Question>(`/api/questions/${id}`, data).then(r => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/api/questions/${id}`).then(() => undefined),

  reorder: (orderedIds: number[]): Promise<void> =>
    api.patch('/api/questions/reorder', { orderedIds }).then(() => undefined),

  uploadFile: (file: File): Promise<string> => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<{ path: string }>('/api/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then(r => r.data.path)
  }
}
