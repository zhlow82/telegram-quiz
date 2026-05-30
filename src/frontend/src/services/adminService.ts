import api from './api'
import type { UserResponse, InvitationCodeResponse, CreateUserRequest } from '@/types/admin'

export const adminService = {
  listUsers: () => api.get<UserResponse[]>('/auth/admin/users'),
  createUser: (data: CreateUserRequest) => api.post<UserResponse>('/auth/admin/users', data),
  updateRole: (id: number, role: string) => api.patch<UserResponse>(`/auth/admin/users/${id}/role`, { role }),
  updateProfile: (id: number, firstName: string, lastName: string) =>
    api.patch<UserResponse>(`/auth/admin/users/${id}/profile`, { firstName, lastName }),
  resetPassword: (id: number, newPassword: string) =>
    api.patch(`/auth/admin/users/${id}/password`, { newPassword }),
  activateUser: (id: number) => api.patch(`/auth/admin/users/${id}/activate`),
  deactivateUser: (id: number) => api.patch(`/auth/admin/users/${id}/deactivate`),
  deleteUser: (id: number) => api.delete(`/auth/admin/users/${id}`),

  listCodes: () => api.get<InvitationCodeResponse[]>('/auth/admin/invitation-codes'),
  generateCode: () => api.post<InvitationCodeResponse>('/auth/admin/invitation-codes'),
  deactivateCode: (id: number) => api.delete(`/auth/admin/invitation-codes/${id}`),
  activateCode: (id: number) => api.patch(`/auth/admin/invitation-codes/${id}/activate`),
  deleteCode: (id: number) => api.delete(`/auth/admin/invitation-codes/${id}/permanent`),

  getGoogleSettings: () => api.get<{ clientId: string; secretConfigured: boolean }>('/auth/admin/settings/google'),
  saveGoogleSettings: (clientId: string, clientSecret: string) =>
    api.put('/auth/admin/settings/google', { clientId, clientSecret }),
}
