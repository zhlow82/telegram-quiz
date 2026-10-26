import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn()
  }
}))

function tokenWith(payload: Record<string, unknown>): string {
  return `header.${btoa(JSON.stringify(payload))}.signature`
}

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('stores the session and exposes admin state after login', async () => {
    const token = tokenWith({
      sub: 'alice',
      role: 'ROLE_ADMIN',
      provider: 'local',
      userId: 7
    })
    vi.mocked(api.post).mockResolvedValue({
      data: { accessToken: token, refreshToken: 'refresh-token' }
    })
    vi.mocked(api.get).mockResolvedValue({
      data: { firstName: 'Alice', lastName: 'Admin' }
    })

    const store = useAuthStore()
    await store.login('alice', 'secret')

    expect(store.accessToken).toBe(token)
    expect(store.username).toBe('alice')
    expect(store.isAdmin).toBe(true)
    expect(store.isLocalAccount).toBe(true)
    expect(store.displayName).toBe('Alice Admin')
    expect(localStorage.getItem('refreshToken')).toBe('refresh-token')
    expect(localStorage.getItem('userRole')).toBe('ROLE_ADMIN')
  })

  it('clears state even when the logout request fails', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('network down'))

    const store = useAuthStore()
    store.accessToken = 'token'
    store.username = 'alice'
    store.role = 'ROLE_ADMIN'
    localStorage.setItem('accessToken', 'token')
    localStorage.setItem('refreshToken', 'refresh-token')
    localStorage.setItem('username', 'alice')

    await expect(store.logout()).rejects.toThrow('network down')

    expect(store.accessToken).toBeNull()
    expect(store.username).toBeNull()
    expect(store.role).toBeNull()
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
  })

  it('falls back to the username when no names are set', () => {
    const store = useAuthStore()
    store.username = 'alice'
    store.firstName = null
    store.lastName = null

    expect(store.displayName).toBe('alice')
  })
})
