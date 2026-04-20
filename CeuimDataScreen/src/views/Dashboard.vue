<template>
  <div class="dashboard-container">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-text">三维城市规划协同管理系统</span>
        </div>
      </div>
      
      <div class="header-right">
        <div class="user-info">
          <el-tag :type="getRoleTagType()" size="small">{{ getRoleText() }}</el-tag>
          <span class="welcome-text">欢迎，{{ authStore.user?.realName || authStore.user?.username }}</span>
          <el-dropdown @command="handleCommand">
            <el-button circle>
              <el-icon><User /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings" v-if="authStore.isSuperAdmin">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <!-- 主要内容区域 -->
    <main class="dashboard-main">
      <div class="content-wrapper">
        <h1 class="page-title">项目管理中心</h1>
        
        <!-- 功能卡片 -->
        <div class="feature-cards">
          <div 
            class="feature-card" 
            v-for="feature in visibleFeatures" 
            :key="feature.title"
            @click="navigateTo(feature.route)"
          >
            <el-icon class="card-icon"><component :is="feature.icon" /></el-icon>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </div>
        
        <!-- 快速统计 -->
        <div class="stats-section">
          <h2>数据概览</h2>
          <div class="stats-grid" v-loading="loadingStats">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalProjects }}</div>
              <div class="stat-label">总项目数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.activeProjects }}</div>
              <div class="stat-label">进行中</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.completedProjects }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalLands }}</div>
              <div class="stat-label">土地储备</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Location, Document, DataAnalysis, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore, RoleType } from '@/stores/auth'
import { projectApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// 所有功能模块
const allFeatures = [
  {
    title: '三维地图',
    description: '查看项目地理分布',
    icon: markRaw(Location),
    route: '/map',
    minRole: RoleType.USER
  },
  {
    title: '项目管理',
    description: '管理项目信息',
    icon: markRaw(Document),
    route: '/projects',
    minRole: RoleType.ADMIN
  },
  {
    title: '数据统计',
    description: '查看统计报表',
    icon: markRaw(DataAnalysis),
    route: '/statistics',
    minRole: RoleType.USER
  },
  {
    title: '用户管理',
    description: '管理系统用户',
    icon: markRaw(UserFilled),
    route: '/admin/users',
    minRole: RoleType.SUPER_ADMIN
  }
]

const visibleFeatures = computed(() => {
  return allFeatures.filter(feature => authStore.hasRole(feature.minRole))
})

const loadingStats = ref(false)
const stats = ref({
  totalProjects: 0,
  activeProjects: 0,
  completedProjects: 0,
  totalLands: 0
})

const getRoleTagType = () => {
  const role = authStore.user?.role
  if (role === RoleType.SUPER_ADMIN) return 'danger'
  if (role === RoleType.ADMIN) return 'warning'
  return 'info'
}

const getRoleText = () => {
  const role = authStore.user?.role
  const map: Record<string, string> = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
    USER: '普通用户'
  }
  return map[role as string] || '用户'
}

const handleCommand = (command: string) => {
  console.log('[Dashboard] 用户菜单操作:', command)
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      authStore.logout().then(() => {
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      break
  }
}

const navigateTo = (route: string): void => {
  console.log('[Dashboard] 导航到:', route)
  router.push(route)
}

const loadStats = async () => {
  console.log('[Dashboard] 加载统计数据')
  loadingStats.value = true
  
  try {
    const response = await projectApi.statistics()
    if (response.code === 200) {
      stats.value = {
        totalProjects: response.data.total || 0,
        activeProjects: response.data.inProgress || 0,
        completedProjects: response.data.completed || 0,
        totalLands: 0
      }
      console.log('[Dashboard] 统计数据:', stats.value)
    }
  } catch (error) {
    console.error('[Dashboard] 加载失败:', error)
  } finally {
    loadingStats.value = false
  }
}

onMounted(() => {
  console.log('[Dashboard] 组件挂载')
  loadStats()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.dashboard-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: #409eff;
  }
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .welcome-text {
      color: #606266;
      font-size: 14px;
    }
  }
}

.dashboard-main {
  padding: 24px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px 0;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.feature-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  }
  
  .card-icon {
    font-size: 32px;
    color: #409eff;
    margin-bottom: 12px;
  }
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }
  
  p {
    font-size: 13px;
    color: #909399;
    margin: 0;
  }
}

.stats-section {
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px 0;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.stat-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  
  .stat-number {
    font-size: 32px;
    font-weight: 700;
    color: #409eff;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}
</style>