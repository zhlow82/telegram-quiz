export function decodeJwtPayload(token: string): Record<string, unknown> {
  try {
    const base64 = token.split('.')[1]
    return JSON.parse(atob(base64.replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return {}
  }
}

export function isTokenExpired(token: string): boolean {
  try {
    const payload = decodeJwtPayload(token)
    return (payload.exp as number) * 1000 < Date.now()
  } catch {
    return true
  }
}
