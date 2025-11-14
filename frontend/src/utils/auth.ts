import Cookies from 'js-cookie'

const tokenKey: string = 'User-Token'

export function getToken(): undefined | string {
  return Cookies.get(tokenKey)
}

export function setToken(token: string) {
  Cookies.set(tokenKey, token)
}

export function removeToken() {
  Cookies.remove(tokenKey)
}

export function isAuthenticated(): boolean {
  if (getToken()) {
    return true
  }

  return false
}
