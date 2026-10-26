import MockAdapter from 'axios-mock-adapter'
import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/utils/jwt', () => ({
  isTokenExpired: () => false
}))

async function loadApi() {
  vi.resetModules()
  const module = await import('@/services/api')
  return module.default
}

describe('api client', () => {
  beforeEach(() => {
    localStorage.clear()
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { href: '/' }
    })
  })

  it('refreshes on 401 and retries the original request', async () => {
    const api = await loadApi()
    const mock = new MockAdapter(api)
    mock.onGet('/protected').replyOnce(401).onGet('/protected').reply(200, { ok: true })
    const refresh = vi.spyOn(axios, 'post').mockResolvedValue({
      data: { accessToken: 'new-token' }
    })
    localStorage.setItem('accessToken', 'old-token')
    localStorage.setItem('refreshToken', 'refresh-token')

    const response = await api.get('/protected')

    expect(response.data).toEqual({ ok: true })
    expect(refresh).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'refresh-token' })
    expect(localStorage.getItem('accessToken')).toBe('new-token')
    mock.restore()
  })

  it('clears the session when the refresh fails', async () => {
    const api = await loadApi()
    const mock = new MockAdapter(api)
    mock.onGet('/protected').reply(401)
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('refresh failed'))
    localStorage.setItem('accessToken', 'old-token')
    localStorage.setItem('refreshToken', 'refresh-token')
    localStorage.setItem('username', 'alice')

    await expect(api.get('/protected')).rejects.toBeTruthy()

    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
    expect(window.location.href).toBe('/login')
    mock.restore()
  })

  it('shares one refresh across parallel 401 responses', async () => {
    const api = await loadApi()
    const mock = new MockAdapter(api)
    let attempts = 0
    mock.onGet(/\/parallel/).reply(() => {
      attempts += 1
      return attempts <= 2 ? [401] : [200, { ok: true }]
    })
    let resolveRefresh!: (value: { data: { accessToken: string } }) => void
    const refresh = vi.spyOn(axios, 'post').mockReturnValue(new Promise((resolve) => {
      resolveRefresh = resolve
    }))
    localStorage.setItem('accessToken', 'old-token')
    localStorage.setItem('refreshToken', 'refresh-token')

    const first = api.get('/parallel/1')
    const second = api.get('/parallel/2')

    await vi.waitFor(() => expect(refresh).toHaveBeenCalledTimes(1))
    resolveRefresh({ data: { accessToken: 'new-token' } })

    const [firstResponse, secondResponse] = await Promise.all([first, second])

    expect(firstResponse.data).toEqual({ ok: true })
    expect(secondResponse.data).toEqual({ ok: true })
    expect(refresh).toHaveBeenCalledTimes(1)
    mock.restore()
  })
})
