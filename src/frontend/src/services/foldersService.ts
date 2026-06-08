import api from './api'
import type { Folder, FolderInviteRequest, FolderMember, FolderRequest } from '@/types/folder'

export const foldersService = {
  list: (): Promise<Folder[]> =>
    api.get<Folder[]>('/api/folders').then(r => r.data),

  create: (data: FolderRequest): Promise<Folder> =>
    api.post<Folder>('/api/folders', data).then(r => r.data),

  rename: (id: number, data: FolderRequest): Promise<Folder> =>
    api.patch<Folder>(`/api/folders/${id}`, data).then(r => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/api/folders/${id}`).then(() => undefined),

  reorder: (orderedIds: number[]): Promise<void> =>
    api.patch('/api/folders/reorder', { orderedIds }).then(() => undefined),

  // Member management
  getMembers: (folderId: number): Promise<FolderMember[]> =>
    api.get<FolderMember[]>(`/api/folders/${folderId}/members`).then(r => r.data),

  inviteUser: (folderId: number, data: FolderInviteRequest): Promise<FolderMember> =>
    api.post<FolderMember>(`/api/folders/${folderId}/members`, data).then(r => r.data),

  removeMember: (folderId: number, username: string): Promise<void> =>
    api.delete(`/api/folders/${folderId}/members/${username}`).then(() => undefined),

  updateMemberRole: (folderId: number, username: string, role: string): Promise<FolderMember> =>
    api.patch<FolderMember>(`/api/folders/${folderId}/members/${username}`, { role }).then(r => r.data),

  // Invitations
  getInvitations: (): Promise<FolderMember[]> =>
    api.get<FolderMember[]>('/api/folders/invitations').then(r => r.data),

  acceptInvitation: (id: number): Promise<void> =>
    api.post(`/api/folders/invitations/${id}/accept`).then(() => undefined),

  declineInvitation: (id: number): Promise<void> =>
    api.post(`/api/folders/invitations/${id}/decline`).then(() => undefined),
}

