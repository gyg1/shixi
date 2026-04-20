<template>
  <div class="admin-layout" :class="{ 'map-fullscreen': isMapPage }">
    <!-- 地图页浮动菜单按钮 -->
    <div v-if="isMapPage" class="floating-menu-btn" @click="sidebarVisible = !sidebarVisible">
      <el-icon size="20"><Operation /></el-icon>
    </div>
    
    <!-- 地图页遮罩 -->
    <div v-if="isMapPage && sidebarVisible" class="sidebar-overlay" @click="sidebarVisible = false"></div>
    
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed, floating: isMapPage, hidden: isMapPage && !sidebarVisible }">
      <div class="sidebar-header">
        <h1 v-if="!isCollapsed">城市规划系统</h1>
        <span v-else>规划</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/map">
          <el-icon><Location /></el-icon>
          <span>三维地图</span>
        </el-menu-item>
        
        <el-sub-menu index="project" v-if="authStore.isAdmin">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>项目管理</span>
          </template>
          <el-menu-item index="/admin/projects">项目列表</el-menu-item>
          <el-menu-item index="/admin/danger-works">危险作业</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/admin/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        
        <el-sub-menu index="system" v-if="authStore.isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/users" v-if="authStore.isAdmin">用户管理</el-menu-item>
          <el-menu-item index="/admin/dicts">字典管理</el-menu-item>
          <el-menu-item index="/admin/form-templates">表单模板</el-menu-item>
          <el-menu-item index="/admin/settings" v-if="authStore.isSuperAdmin">系统设置</el-menu-item>
        </el-sub-menu>
      </el-menu>
      
      <div class="sidebar-footer" v-if="!isMapPage">
        <el-button text @click="toggleCollapse">
          <el-icon><component :is="isCollapsed ? 'DArrowRight' : 'DArrowLeft'" /></el-icon>
        </el-button>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-area">
      <!-- 顶部栏 (地图页隐藏) -->
      <header class="topbar" v-if="!isMapPage">
        <div class="topbar-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="topbar-right">
          <el-tag :type="getRoleTagType()" size="small">{{ getRoleText() }}</el-tag>
          <span class="username">{{ authStore.user?.realName || authStore.user?.username }}</span>
          <el-dropdown @command="handleCommand">
            <el-button circle size="small">
              <el-icon><User /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区 -->
      <main class="content" :class="{ 'content-fullscreen': isMapPage }">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  House, Location, Document, DataAnalysis, Setting, User,
  DArrowLeft, DArrowRight, Operation 
} from '@element-plus/icons-vue'
import { useAuthStore, RoleType } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const sidebarVisible = ref(false)

// 检测是否为地图页面
const isMapPage = computed(() => route.path === '/admin/map')

// 离开地图页时自动隐藏侧边栏浮层
watch(isMapPage, (val) => {
  if (!val) sidebarVisible.value = false
})

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/dashboard': '首页',
    '/admin/map': '三维地图',
    '/admin/projects': '项目列表',
    '/admin/projects/create': '新建项目',
    '/admin/statistics': '数据统计',
    '/admin/users': '用户管理',
    '/admin/dicts': '字典管理',
    '/admin/form-templates': '表单模板',
    '/admin/settings': '系统设置'
  }
  return titles[route.path] || '管理后台'
})

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

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
  if (command === 'logout') {
    authStore.logout().then(() => {
      ElMessage.success('已退出')
      router.push('/login')
    })
  } else if (command === 'profile') {
    ElMessage.info('功能开发中')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  
  /* 地图全屏模式 */
  &.map-fullscreen {
    .main-area {
      margin-left: 0;
    }
  }
}

/* 浮动菜单按钮 */
.floating-menu-btn {
  position: fixed;
  top: 80px;
  left: 12px;
  z-index: 2000;
  width: 40px;
  height: 40px;
  background: rgba(10, 20, 40, 0.85);
  border: 1px solid rgba(0, 180, 255, 0.4);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #00e5ff;
  transition: all 0.3s;
  backdrop-filter: blur(8px);
  
  &:hover {
    background: rgba(0, 180, 255, 0.25);
    transform: scale(1.05);
  }
}

/* 遮罩 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 2001;
}

.sidebar {
  width: 220px;
  background: #304156;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  
  &.collapsed {
    width: 64px;
  }
  
  /* 地图页浮动侧边栏 */
  &.floating {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 2002;
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.4);
    transform: translateX(0);
    transition: transform 0.3s ease;
    
    &.hidden {
      transform: translateX(-100%);
    }
  }
}

.sidebar-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #263445;
  color: #fff;
  
  h1 {
    font-size: 16px;
    margin: 0;
    white-space: nowrap;
  }
  
  span {
    font-size: 14px;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    color: #bfcbd9;
    
    &:hover {
      background: #263445;
    }
  }
  
  :deep(.el-menu-item.is-active) {
    color: #409eff;
    background: #263445;
  }

  // 子菜单背景和文字颜色
  :deep(.el-sub-menu .el-menu) {
    background-color: #1f2d3d;
  }

  :deep(.el-sub-menu .el-menu .el-menu-item) {
    color: #bfcbd9;
    background-color: #1f2d3d;

    &:hover {
      background: #263445;
    }

    &.is-active {
      color: #409eff;
      background: #263445;
    }
  }
}

.sidebar-footer {
  padding: 10px;
  text-align: center;
  border-top: 1px solid #263445;
  
  .el-button {
    color: #bfcbd9;
  }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.topbar-left {
  .page-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .username {
    font-size: 14px;
    color: #606266;
  }
}

.content {
  flex: 1;
  overflow: auto;
  
  &.content-fullscreen {
    overflow: hidden;
  }
}
</style>
