import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

// 用户角色枚举
export enum RoleType {
  USER = 'USER',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN'
}

export interface User {
  id: number
  username: string
  email: string
  phone?: string
  realName?: string
  avatar?: string
  role: RoleType | string
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  phone?: string
  realName?: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: User
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const isLoggedIn = ref<boolean>(!!token.value)

  // 计算属性 - 角色判断
  const isUser = computed(() => user.value?.role === RoleType.USER)
  const isAdmin = computed(() => 
    user.value?.role === RoleType.ADMIN || user.value?.role === RoleType.SUPER_ADMIN
  )
  const isSuperAdmin = computed(() => user.value?.role === RoleType.SUPER_ADMIN)

  // 配置axios基础URL
  const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000
  })

  // 请求拦截器
  api.interceptors.request.use(
    (config) => {
      if (token.value) {
        config.headers.Authorization = `Bearer ${token.value}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  api.interceptors.response.use(
    (response) => {
      return response
    },
    (error) => {
      if (error.response?.status === 401) {
        logout()
      }
      return Promise.reject(error)
    }
  )

  const login = (loginData: LoginRequest) => {
    return api.post<ApiResponse<LoginResponse>>('/auth/login', loginData)
      .then(response => {
        if (response.data.code === 200) {
          token.value = response.data.data.token
          user.value = response.data.data.user
          isLoggedIn.value = true
          localStorage.setItem('token', token.value)
          localStorage.setItem('user', JSON.stringify(user.value))
          return { success: true, message: response.data.message || '登录成功' }
        } else {
          return { success: false, message: response.data.message || '登录失败' }
        }
      })
      .catch((error: any) => {
        console.error('Login error:', error)
        return { 
          success: false, 
          message: error.response?.data?.message || '登录失败，请检查网络连接' 
        }
      })
  }

  const register = (registerData: RegisterRequest) => {
    return api.post<ApiResponse<User>>('/auth/register', registerData)
      .then(response => {
        if (response.data.code === 200) {
          return { success: true, message: response.data.message || '注册成功' }
        } else {
          return { success: false, message: response.data.message || '注册失败' }
        }
      })
      .catch((error: any) => {
        console.error('Register error:', error)
        return { 
          success: false, 
          message: error.response?.data?.message || '注册失败，请检查网络连接' 
        }
      })
  }

  const logout = () => {
    token.value = null
    user.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return Promise.resolve()
  }

  const getCurrentUser = () => {
    return api.get<ApiResponse<User>>('/auth/me')
      .then(response => {
        if (response.data.code === 200) {
          user.value = response.data.data
          localStorage.setItem('user', JSON.stringify(user.value))
          return true
        }
        return false
      })
      .catch(error => {
        console.error('Get current user error:', error)
        logout()
        return false
      })
  }

  const validateToken = () => {
    if (!token.value) return Promise.resolve(false)
    
    return api.post<ApiResponse<boolean>>('/auth/validate')
      .then(response => {
        if (response.data.code === 200 && response.data.data) {
          return true
        }
        logout()
        return false
      })
      .catch(() => {
        logout()
        return false
      })
  }

  // 初始化时恢复用户信息
  const initializeAuth = () => {
    const savedUser = localStorage.getItem('user')
    if (savedUser && token.value) {
      try {
        user.value = JSON.parse(savedUser)
        isLoggedIn.value = true
        // 验证token有效性
        getCurrentUser()
      } catch (e) {
        logout()
      }
    }
  }

  // 权限检查
  const hasRole = (requiredRole: RoleType): boolean => {
    if (!user.value) return false
    const roleHierarchy = { USER: 0, ADMIN: 1, SUPER_ADMIN: 2 }
    const userLevel = roleHierarchy[user.value.role as keyof typeof roleHierarchy] ?? 0
    const requiredLevel = roleHierarchy[requiredRole]
    return userLevel >= requiredLevel
  }

  return {
    user,
    token,
    isLoggedIn,
    isUser,
    isAdmin,
    isSuperAdmin,
    api,
    login,
    register,
    logout,
    getCurrentUser,
    validateToken,
    initializeAuth,
    hasRole
  }
})