import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await axios.post('/api/v1/auth/login', { username, password })
    const data = res.data.data
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo
    permissions.value = data.userInfo.permissions || []
    roles.value = data.userInfo.roles || []

    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    localStorage.setItem('permissions', JSON.stringify(data.userInfo.permissions || []))
    localStorage.setItem('roles', JSON.stringify(data.userInfo.roles || []))
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    permissions.value = []
    roles.value = []
    localStorage.clear()
  }

  function hasPermission(perm) {
    return permissions.value.includes(perm)
  }

  function hasRole(role) {
    return roles.value.includes(role)
  }

  return { token, refreshToken, userInfo, permissions, roles, isLoggedIn, login, logout, hasPermission, hasRole }
})
