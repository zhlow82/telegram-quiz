import api from './api'
import type { UserResponse, InvitationCodeResponse, CreateUserRequest } from '@/types/admin'

export const adminService = {
  listUsers: async (): Promise<UserResponse[]> => {
    const { data } = await api.get<UserResponse[]>('/auth/admin/users')
    return data
  },
  createUser: async (data: CreateUserRequest): Promise<UserResponse> => {
    const { data: result } = await api.post<UserResponse>('/auth/admin/users', data)
    return result
  },
  updateRole: async (id: number, role: string): Promise<UserResponse> => {
    const { data } = await api.patch<UserResponse>(`/auth/admin/users/${id}/role`, { role })
    return data
  },
  updateProfile: async (id: number, firstName: string, lastName: string): Promise<UserResponse> => {
    const { data } = await api.patch<UserResponse>(`/auth/admin/users/${id}/profile`, { firstName, lastName })
    return data
  },
  resetPassword: async (id: number, newPassword: string): Promise<void> => {
    await api.patch(`/auth/admin/users/${id}/password`, { newPassword })
  },
  activateUser: async (id: number): Promise<void> => {
    await api.patch(`/auth/admin/users/${id}/activate`)
  },
  deactivateUser: async (id: number): Promise<void> => {
    await api.patch(`/auth/admin/users/${id}/deactivate`)
  },
  deleteUser: async (id: number): Promise<void> => {
    await api.delete(`/auth/admin/users/${id}`)
  },

  listCodes: async (): Promise<InvitationCodeResponse[]> => {
    const { data } = await api.get<InvitationCodeResponse[]>('/auth/admin/invitation-codes')
    return data
  },
  generateCode: async (): Promise<InvitationCodeResponse> => {
    const { data } = await api.post<InvitationCodeResponse>('/auth/admin/invitation-codes')
    return data
  },
  deactivateCode: async (id: number): Promise<void> => {
    await api.delete(`/auth/admin/invitation-codes/${id}`)
  },
  activateCode: async (id: number): Promise<void> => {
    await api.patch(`/auth/admin/invitation-codes/${id}/activate`)
  },
  deleteCode: async (id: number): Promise<void> => {
    await api.delete(`/auth/admin/invitation-codes/${id}/permanent`)
  },

  getGoogleSettings: async () => {
    const { data } = await api.get<{ clientId: string; secretConfigured: boolean }>('/auth/admin/settings/google')
    return data
  },
  saveGoogleSettings: async (clientId: string, clientSecret: string): Promise<void> => {
    await api.put('/auth/admin/settings/google', { clientId, clientSecret })
  },

  getBrandingSettings: async () => {
    const { data } = await api.get<{ appName: string; loginWelcomeText: string; appLogoUrl: string | null }>('/auth/settings/branding')
    return data
  },
  saveBrandingSettings: async (data: { appName: string; loginWelcomeText: string; appLogoBlobId: string | null }): Promise<void> => {
    await api.put('/auth/admin/settings/branding', data)
  },

  getLogs: async (params?: { level?: string; search?: string; limit?: number }) => {
    const { data } = await api.get<LogEntry[]>('/auth/admin/logs', { params })
    return data
  },

  getMainServiceLogs: async (params?: { level?: string; search?: string; limit?: number }) => {
    const { data } = await api.get<LogEntry[]>('/api/admin/logs', { params })
    return data
  },
}

export interface LogEntry {
  timestamp: number
  level: string
  logger: string
  message: string
  formattedTimestamp: string
}
