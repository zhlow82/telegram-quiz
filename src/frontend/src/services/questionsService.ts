import api from './api'
import type { Question, QuestionRequest, ExportData, ImportResult } from '@/types/question'

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

  assignFolder: (id: number, folderId: number | null): Promise<Question> =>
    api.patch<Question>(`/api/questions/${id}/folder`, { folderId }).then(r => r.data),

  uploadFile: (file: File): Promise<string> => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<{ path: string }>('/api/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then(r => r.data.path)
  },

  exportQuestions: (questionIds: number[], includeImages: boolean): Promise<ExportData> =>
    api.post<ExportData>('/api/questions/export', { questionIds, includeImages }).then(r => r.data),

  importQuestions: (data: ExportData): Promise<ImportResult> =>
    api.post<ImportResult>('/api/questions/import', data).then(r => r.data)
}
