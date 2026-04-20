<template>
  <div class="project-list">
    <header class="page-header">
      <h1>项目管理</h1>
      <el-button v-if="authStore.isAdmin" type="primary" @click="createProject">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </header>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索项目名称/编号/地址"
        clearable
        style="width: 260px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filterCategory" placeholder="大类" clearable style="width: 140px" @change="handleCategoryChange">
        <el-option v-for="item in categories" :key="item.itemCode" :label="item.itemName" :value="item.itemCode" />
      </el-select>
      <el-select v-model="filterType" placeholder="子类" clearable style="width: 160px" @change="handleSearch">
        <el-option v-for="item in filterSubTypes" :key="item.itemCode" :label="item.itemName" :value="item.itemName" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
        <el-option label="待启动" value="PENDING" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="projects" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="projectCode" label="编号" width="120" />
      <el-table-column prop="projectName" label="项目名称" min-width="180" />
      <el-table-column label="大类" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.category" :type="row.category === 'LAND' ? 'warning' : 'primary'" size="small">
            {{ getCategoryText(row.category) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="projectType" label="子类" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="district" label="区县" width="100" />
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column v-if="authStore.isAdmin" label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <el-button text type="primary" size="small" @click="viewProject(row.id!)">查看</el-button>
            <el-button text type="primary" size="small" @click="editProject(row.id!)">编辑</el-button>
            <el-button text type="danger" size="small" @click="deleteProject(row.id!)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchProjects"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="detailDialogVisible" title="项目详情" width="760px">
      <template v-if="currentProject">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目编号">{{ currentProject.projectCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ currentProject.projectName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="大类">{{ getCategoryText(currentProject.category) }}</el-descriptions-item>
          <el-descriptions-item label="子类">{{ currentProject.projectType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentProject.status)" size="small">{{ getStatusText(currentProject.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="区县">{{ currentProject.district || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ currentProject.startDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ currentProject.endDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentProject.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentProject.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="空间边界">{{ currentProject.geometry ? '已配置' : '未配置' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentProject.createdAt) }}</el-descriptions-item>
          <el-descriptions-item v-for="item in extraDataEntries" :key="item.key" :label="item.key">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">项目附件</el-divider>
        <el-table v-if="currentProjectFiles.length" :data="currentProjectFiles" size="small" border>
          <el-table-column prop="fileName" label="文件名" min-width="220" />
          <el-table-column prop="fileType" label="类型" width="120" />
          <el-table-column label="大小" width="120">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click="downloadFile(row.id!)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无附件" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { dictApi, projectApi, projectFileApi, type Project, type ProjectFile, type SysDictItem } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const projects = ref<Project[]>([])
const searchKeyword = ref('')
const filterCategory = ref('')
const filterType = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categories = ref<SysDictItem[]>([])
const filterSubTypes = ref<SysDictItem[]>([])

const detailDialogVisible = ref(false)
const currentProject = ref<Project | null>(null)
const currentProjectFiles = ref<ProjectFile[]>([])

const parseExtraData = (value?: string): Record<string, any> => {
  if (!value) return {}
  try {
    return JSON.parse(value)
  } catch {
    return {}
  }
}

const extraDataEntries = computed(() => {
  return Object.entries(parseExtraData(currentProject.value?.extraData)).map(([key, value]) => ({
    key,
    value: value ?? '-'
  }))
})

const getCategoryText = (category?: string) => {
  const map: Record<string, string> = {
    LAND: '土地储备',
    ENGINEERING: '工程项目',
    DISTRICT: '片区项目'
  }
  return category ? (map[category] || category) : '-'
}

const getStatusType = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return status ? (map[status] || 'info') : 'info'
}

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待启动',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return status ? (map[status] || status) : '-'
}

const formatDateTime = (value?: string) => {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

const formatFileSize = (size?: number) => {
  if (!size) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const loadCategories = async () => {
  try {
    const res = await dictApi.listItemsByCode('project_category')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

const loadFilterSubTypes = async (category: string) => {
  if (!category) {
    filterSubTypes.value = []
    return
  }
  const dictCode = category === 'LAND' ? 'land_sub_type' : 'engineering_sub_type'
  try {
    const res = await dictApi.listItemsByCode(dictCode)
    if (res.code === 200) {
      filterSubTypes.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await projectApi.list({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      category: filterCategory.value || undefined,
      projectType: filterType.value || undefined,
      status: filterStatus.value || undefined
    })
    if (response.code === 200) {
      projects.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载项目列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载项目列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

const handleReset = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  filterType.value = ''
  filterStatus.value = ''
  filterSubTypes.value = []
  currentPage.value = 1
  fetchProjects()
}

const handleCategoryChange = async (value: string) => {
  filterType.value = ''
  currentPage.value = 1
  await loadFilterSubTypes(value)
  fetchProjects()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchProjects()
}

const createProject = () => router.push('/admin/projects/create')

const viewProject = async (id: number) => {
  try {
    const [projectRes, fileRes] = await Promise.all([
      projectApi.get(id),
      projectFileApi.list(id)
    ])
    if (projectRes.code === 200) {
      currentProject.value = projectRes.data
      currentProjectFiles.value = fileRes.code === 200 ? (fileRes.data || []) : []
      detailDialogVisible.value = true
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取项目详情失败')
  }
}

const editProject = (id: number) => router.push(`/admin/projects/${id}/edit`)

const deleteProject = (id: number) => {
  ElMessageBox.confirm('确定删除该项目吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const response = await projectApi.delete(id)
        if (response.code === 200) {
          ElMessage.success('删除成功')
          if (projects.value.length === 1 && currentPage.value > 1) {
            currentPage.value -= 1
          }
          fetchProjects()
        }
      } catch (error: any) {
        ElMessage.error(error.message || '删除失败')
      }
    })
    .catch(() => {})
}

const downloadFile = (fileId: number) => {
  window.open(projectFileApi.downloadUrl(fileId), '_blank')
}

onMounted(() => {
  loadCategories()
  fetchProjects()
})
</script>

<style scoped lang="scss">
.project-list {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h1 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  border-radius: 8px;
}

.action-btns {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
}
</style>
