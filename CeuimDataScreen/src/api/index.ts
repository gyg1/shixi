import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = 'http://localhost:8080/api'

/**
 * API 服务 - 统一处理后端请求
 */
class ApiService {
  private getHeaders(isMultipart: boolean = false): HeadersInit {
    const authStore = useAuthStore()
    const headers: HeadersInit = {}
    
    if (!isMultipart) {
      headers['Content-Type'] = 'application/json'
    }
    
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return headers
  }

  private async request<T>(url: string, options: RequestInit = {}): Promise<T> {
    const fullUrl = `${API_BASE_URL}${url}`
    const isMultipart = options.body instanceof FormData
    
    let parsedBody = ''
    if (options.body && !isMultipart) {
      try {
        parsedBody = JSON.parse(options.body as string)
      } catch (e) {
        parsedBody = options.body as string
      }
    }
    
    console.log(`[API] ${options.method || 'GET'} ${url}`, isMultipart ? '[FormData]' : parsedBody)
    
    try {
      
      // Combine headers
      const finalHeaders: HeadersInit = {
        ...this.getHeaders(isMultipart),
        ...options.headers
      }
      
      // CRITICAL: When using FormData, we MUST NOT set the Content-Type header at all.
      // If we set it (even to undefined or null), the browser won't inject the boundary.
      // So we must actively delete it from our combined Record if it exists.
      if (isMultipart && (finalHeaders as any)['Content-Type']) {
        console.log('[API_DEBUG] 检测到 FormData，从 finalHeaders 移除 Content-Type', (finalHeaders as any)['Content-Type'])
        delete (finalHeaders as any)['Content-Type']
      }

      console.log('[API_DEBUG] 准备传入 fetch 的参数:', {
        url: fullUrl,
        method: options.method,
        finalHeaders: { ...finalHeaders }, // cloned for log
        bodyIsFormData: isMultipart,
      })

      const response = await fetch(fullUrl, {
        ...options,
        headers: finalHeaders
      })

      const data = await response.json()
      console.log(`[API] 响应:`, data)

      if (!response.ok) {
        console.error(`[API] 请求失败 - ${response.status}:`, data.message)
        throw new Error(data.message || '请求失败')
      }

      return data
    } catch (error) {
      console.error(`[API] 请求异常:`, error)
      throw error
    }
  }

  // GET 请求
  async get<T>(url: string, params?: Record<string, any>): Promise<T> {
    let queryString = ''
    if (params) {
      const searchParams = new URLSearchParams()
      Object.keys(params).forEach(key => {
        if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
          searchParams.append(key, params[key])
        }
      })
      queryString = searchParams.toString()
      if (queryString) queryString = '?' + queryString
    }
    return this.request<T>(`${url}${queryString}`, { method: 'GET' })
  }

  // POST 请求
  async post<T>(url: string, data?: any): Promise<T> {
    const isFormData = data instanceof FormData
    return this.request<T>(url, {
      method: 'POST',
      body: isFormData ? data : (data ? JSON.stringify(data) : undefined)
    })
  }

  // PUT 请求
  async put<T>(url: string, data?: any): Promise<T> {
    return this.request<T>(url, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  // DELETE 请求
  async delete<T>(url: string): Promise<T> {
    return this.request<T>(url, { method: 'DELETE' })
  }
}

export const apiService = new ApiService()

// ==================== 项目管理 API ====================

export interface Project {
  id?: number
  projectCode?: string
  projectName: string
  projectType?: string
  category?: string
  subType?: string
  extraData?: string
  area?: number
  totalInvestment?: number
  district?: string
  status?: string
  startDate?: string
  endDate?: string
  description?: string
  address?: string
  geometry?: string
  createdBy?: number
  createdAt?: string
  updatedAt?: string
  secondaryGroup?: string
  constructionUnit?: string
  developmentUnit?: string
  fundSource?: string
  proceduresStatus?: string
  completionProcedures?: string
  existingProblems?: string
  currentInvestment?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export const projectApi = {
  // 分页查询项目
  list: (params: { pageNum?: number; pageSize?: number; keyword?: string; category?: string; projectType?: string; status?: string }) => {
    console.log('[项目API] 查询列表', params)
    return apiService.get<ApiResponse<PageResult<Project>>>('/projects', params)
  },

  // 获取详情
  get: (id: number) => {
    console.log('[项目API] 获取详情', id)
    return apiService.get<ApiResponse<Project>>(`/projects/${id}`)
  },

  // 创建
  create: (project: Project) => {
    console.log('[项目API] 创建项目', project)
    return apiService.post<ApiResponse<Project>>('/projects', project)
  },

  // 更新
  update: (id: number, project: Project) => {
    console.log('[项目API] 更新项目', id, project)
    return apiService.put<ApiResponse<Project>>(`/projects/${id}`, project)
  },

  // 删除
  delete: (id: number) => {
    console.log('[项目API] 删除项目', id)
    return apiService.delete<ApiResponse<void>>(`/projects/${id}`)
  },

  // 获取统计
  statistics: () => {
    console.log('[项目API] 获取统计')
    return apiService.get<ApiResponse<Record<string, number>>>('/projects/statistics')
  },

  // 获取所有 (地图显示)
  getAll: () => {
    return apiService.get<ApiResponse<Project[]>>('/projects/all')
  },

  // 根据大类获取项目 (含 geometry)
  getByCategory: (category: string) => {
    return apiService.get<ApiResponse<Project[]>>(`/projects/category/${category}`)
  },

  // 根据大类统计各子类
  getCategoryStats: (category: string) => {
    return apiService.get<ApiResponse<Array<{subType: string, count: number, totalArea: number}>>>(`/projects/category/${category}/stats`)
  }
}

// ==================== 项目文件 API ====================

export interface ProjectFile {
  id?: number
  projectId?: number
  fileName?: string
  filePath?: string
  fileSize?: number
  fileType?: string
  uploadedBy?: number
  createdAt?: string
}

export const projectFileApi = {
  // 上传文件
  upload: (projectId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiService.post<ApiResponse<ProjectFile>>(`/projects/${projectId}/files`, formData)
  },

  // 获取文件列表
  list: (projectId: number) => {
    return apiService.get<ApiResponse<ProjectFile[]>>(`/projects/${projectId}/files`)
  },

  // 下载文件
  downloadUrl: (fileId: number) => {
    return `http://localhost:8080/api/projects/files/${fileId}/download`
  },

  // 删除文件
  delete: (fileId: number) => {
    return apiService.delete<ApiResponse<void>>(`/projects/files/${fileId}`)
  }
}

// ==================== 土地储备 API ====================

export interface LandReserve {
  id?: number
  landCode?: string
  landName: string
  landUse?: string
  area?: number
  price?: number
  status?: string
  geometry?: string
  createdAt?: string
  updatedAt?: string
}

export const landApi = {
  // 分页查询
  list: (params: { pageNum?: number; pageSize?: number; keyword?: string; landUse?: string; status?: string }) => {
    console.log('[土地API] 查询列表', params)
    return apiService.get<ApiResponse<PageResult<LandReserve>>>('/lands', params)
  },

  // 获取所有 (地图显示)
  getAll: () => {
    console.log('[土地API] 获取全部')
    return apiService.get<ApiResponse<LandReserve[]>>('/lands/all')
  },

  // 获取详情
  get: (id: number) => {
    console.log('[土地API] 获取详情', id)
    return apiService.get<ApiResponse<LandReserve>>(`/lands/${id}`)
  },

  // 创建
  create: (land: LandReserve) => {
    console.log('[土地API] 创建地块', land)
    return apiService.post<ApiResponse<LandReserve>>('/lands', land)
  },

  // 更新
  update: (id: number, land: LandReserve) => {
    console.log('[土地API] 更新地块', id, land)
    return apiService.put<ApiResponse<LandReserve>>(`/lands/${id}`, land)
  },

  // 删除
  delete: (id: number) => {
    console.log('[土地API] 删除地块', id)
    return apiService.delete<ApiResponse<void>>(`/lands/${id}`)
  }
}

// ==================== 房产信息 API ====================

export interface Property {
  id?: number
  propertyCode?: string
  propertyName: string
  propertyType?: string
  area?: number
  price?: number
  status?: string
  geometry?: string
  createdAt?: string
  updatedAt?: string
}

export const propertyApi = {
  // 分页查询
  list: (params: { pageNum?: number; pageSize?: number; keyword?: string; propertyType?: string }) => {
    console.log('[房产API] 查询列表', params)
    return apiService.get<ApiResponse<PageResult<Property>>>('/properties', params)
  },

  // 获取所有 (地图显示)
  getAll: () => {
    console.log('[房产API] 获取全部')
    return apiService.get<ApiResponse<Property[]>>('/properties/all')
  },

  // 获取详情
  get: (id: number) => {
    console.log('[房产API] 获取详情', id)
    return apiService.get<ApiResponse<Property>>(`/properties/${id}`)
  },

  // 创建
  create: (property: Property) => {
    console.log('[房产API] 创建房产', property)
    return apiService.post<ApiResponse<Property>>('/properties', property)
  },

  // 更新
  update: (id: number, property: Property) => {
    console.log('[房产API] 更新房产', id, property)
    return apiService.put<ApiResponse<Property>>(`/properties/${id}`, property)
  },

  // 删除
  delete: (id: number) => {
    console.log('[房产API] 删除房产', id)
    return apiService.delete<ApiResponse<void>>(`/properties/${id}`)
  },

  // 获取统计
  statistics: () => {
    console.log('[房产API] 获取统计')
    return apiService.get<ApiResponse<Record<string, number>>>('/properties/statistics')
  }
}

// ==================== 字典管理 API ====================

export interface SysDict {
  id?: number
  dictCode: string
  dictName: string
  description?: string
  createdAt?: string
  updatedAt?: string
}

export interface SysDictItem {
  id?: number
  dictId?: number
  itemCode: string
  itemName: string
  parentId?: number
  sort?: number
  color?: string
  status?: string
  createdAt?: string
}

export const dictApi = {
  // 字典类型
  listDicts: () => apiService.get<ApiResponse<SysDict[]>>('/dicts'),
  getDict: (id: number) => apiService.get<ApiResponse<SysDict>>(`/dicts/${id}`),
  getDictByCode: (code: string) => apiService.get<ApiResponse<SysDict>>(`/dicts/code/${code}`),
  createDict: (dict: SysDict) => apiService.post<ApiResponse<SysDict>>('/dicts', dict),
  updateDict: (id: number, dict: SysDict) => apiService.put<ApiResponse<SysDict>>(`/dicts/${id}`, dict),
  deleteDict: (id: number) => apiService.delete<ApiResponse<void>>(`/dicts/${id}`),

  // 字典项
  listItems: (dictId: number) => apiService.get<ApiResponse<SysDictItem[]>>(`/dicts/${dictId}/items`),
  listItemsByCode: (dictCode: string) => apiService.get<ApiResponse<SysDictItem[]>>(`/dicts/code/${dictCode}/items`),
  createItem: (dictId: number, item: SysDictItem) => apiService.post<ApiResponse<SysDictItem>>(`/dicts/${dictId}/items`, item),
  updateItem: (id: number, item: SysDictItem) => apiService.put<ApiResponse<SysDictItem>>(`/dicts/items/${id}`, item),
  deleteItem: (id: number) => apiService.delete<ApiResponse<void>>(`/dicts/items/${id}`)
}

// ==================== 表单模板 API ====================

export interface FormTemplate {
  id?: number
  templateName: string
  category: string
  subType: string
  fields: string  // JSON string of field configs
  createdAt?: string
  updatedAt?: string
}

export interface TemplateField {
  key: string
  label: string
  type: 'text' | 'number' | 'select' | 'date' | 'textarea'
  required?: boolean
  unit?: string
  options?: string[]  // for select type
  placeholder?: string
}

export const formTemplateApi = {
  list: () => apiService.get<ApiResponse<FormTemplate[]>>('/form-templates'),
  get: (id: number) => apiService.get<ApiResponse<FormTemplate>>(`/form-templates/${id}`),
  match: (category: string, subType: string) =>
    apiService.get<ApiResponse<FormTemplate>>('/form-templates/match', { category, subType }),
  create: (t: FormTemplate) => apiService.post<ApiResponse<FormTemplate>>('/form-templates', t),
  update: (id: number, t: FormTemplate) => apiService.put<ApiResponse<FormTemplate>>(`/form-templates/${id}`, t),
  delete: (id: number) => apiService.delete<ApiResponse<void>>(`/form-templates/${id}`)
}

// ==================== 危险作业 API ====================

export interface DangerousWork {
  id?: number
  workCode?: string
  districtProjectId?: number
  districtName?: string
  workType?: string
  workName?: string
  riskLevel?: string
  applicant?: string
  responsiblePerson?: string
  implementor?: string
  locationDesc?: string
  lng?: number
  lat?: number
  planStartTime?: string
  planEndTime?: string
  safetyMeasures?: string
  workDescription?: string
  status?: string
  monitorCode?: string
  createdAt?: string
  updatedAt?: string
}

export const dangerousWorkApi = {
  // 申报（提交即上线）
  apply: (work: DangerousWork) =>
    apiService.post<ApiResponse<DangerousWork>>('/dangerous-works', work),

  // 查询列表
  list: (params?: { status?: string; workType?: string }) =>
    apiService.get<ApiResponse<DangerousWork[]>>('/dangerous-works', params),

  // 查询详情
  getById: (id: number) =>
    apiService.get<ApiResponse<DangerousWork>>(`/dangerous-works/${id}`),

  // 更新状态
  updateStatus: (id: number, status: string) =>
    apiService.put<ApiResponse<string>>(`/dangerous-works/${id}/status?status=${status}`),

  // 地图监控点（进行中）
  getMonitors: () =>
    apiService.get<ApiResponse<DangerousWork[]>>('/dangerous-works/monitors'),

  // 大屏统计数据
  getStatistics: () =>
    apiService.get<ApiResponse<Record<string, any>>>('/dangerous-works/statistics'),
}

export interface SystemUser {
  id?: number
  username: string
  password?: string
  email?: string
  phone?: string
  realName?: string
  avatar?: string
  role?: string
  enabled?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface UserListParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  role?: string
}

export const userApi = {
  list: (params: UserListParams) =>
    apiService.get<ApiResponse<PageResult<SystemUser>>>('/users', params),
  get: (id: number) =>
    apiService.get<ApiResponse<SystemUser>>(`/users/${id}`),
  create: (user: SystemUser) =>
    apiService.post<ApiResponse<SystemUser>>('/users', user),
  update: (id: number, user: SystemUser) =>
    apiService.put<ApiResponse<SystemUser>>(`/users/${id}`, user),
  updateRole: (id: number, role: string) =>
    apiService.put<ApiResponse<void>>(`/users/${id}/role`, { role }),
  resetPassword: (id: number, password: string) =>
    apiService.put<ApiResponse<void>>(`/users/${id}/password`, { password }),
  toggleStatus: (id: number) =>
    apiService.put<ApiResponse<void>>(`/users/${id}/toggle-status`),
  delete: (id: number) =>
    apiService.delete<ApiResponse<void>>(`/users/${id}`)
}

export default apiService
