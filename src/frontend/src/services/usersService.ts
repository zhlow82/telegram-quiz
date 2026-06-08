import api from './api'

export interface UserSearchResult {
  username: string
  firstName: string
  lastName: string
}

export const usersService = {
  search: (q: string): Promise<UserSearchResult[]> =>
    api.get<UserSearchResult[]>('/auth/users/search', { params: { q } }).then(r => r.data),
}
