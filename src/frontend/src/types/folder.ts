export interface Folder {
  id: number
  name: string
  createdBy: string
  createdAt: string
  role: 'OWNER' | 'CO_OWNER' | 'CONTRIBUTOR'
}

export interface FolderRequest {
  name: string
}

export interface FolderMember {
  id: number
  folderId: number
  folderName: string | null
  username: string
  role: 'CO_OWNER' | 'CONTRIBUTOR'
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
  invitedBy: string
  createdAt: string
}

export interface FolderInviteRequest {
  username: string
  role: 'CO_OWNER' | 'CONTRIBUTOR'
}
