export interface UserResponse {
  id: number
  username: string
  firstName: string | null
  lastName: string | null
  email: string | null
  provider: 'local' | 'google'
  role: 'ROLE_ADMIN' | 'ROLE_MEMBER'
  active: boolean
}

export interface InvitationCodeResponse {
  id: number
  code: string
  createdBy: string
  createdAt: string
  active: boolean
}

export interface CreateUserRequest {
  username: string
  password: string
  firstName?: string
  lastName?: string
}
