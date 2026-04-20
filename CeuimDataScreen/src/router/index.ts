import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore, RoleType } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    // 普通用户 - 全屏地图视图
    {
      path: '/user',
      name: 'user-map',
      component: () => import('@/views/UserMapView.vue'),
      meta: { requiresAuth: true }
    },
    // 管理员布局
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiredRole: RoleType.ADMIN },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/AdminDashboard.vue')
        },
        {
          path: 'map',
          name: 'admin-map',
          component: () => import('@/views/admin/AdminMap.vue')
        },
        {
          path: 'projects',
          name: 'admin-projects',
          component: () => import('@/views/project/ProjectList.vue')
        },
        {
          path: 'projects/create',
          name: 'admin-project-create',
          component: () => import('@/views/project/ProjectForm.vue')
        },
        {
          path: 'projects/:id/edit',
          name: 'admin-project-edit',
          component: () => import('@/views/project/ProjectForm.vue')
        },
        {
          path: 'statistics',
          name: 'admin-statistics',
          component: () => import('@/views/statistics/StatisticsView.vue')
        },
        {
          path: 'danger-works',
          name: 'admin-danger-works',
          component: () => import('@/views/project/DangerWorkList.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: { requiredRole: RoleType.ADMIN }
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('@/views/admin/SystemSettings.vue'),
          meta: { requiredRole: RoleType.SUPER_ADMIN }
        },
        {
          path: 'dicts',
          name: 'admin-dicts',
          component: () => import('@/views/admin/DictManagement.vue'),
          meta: { requiredRole: RoleType.ADMIN }
        },
        {
          path: 'form-templates',
          name: 'admin-form-templates',
          component: () => import('@/views/admin/FormTemplateManagement.vue'),
          meta: { requiredRole: RoleType.ADMIN }
        }
      ]
    },
    // 旧路由兼容 - 重定向
    {
      path: '/dashboard',
      redirect: '/admin/dashboard'
    },
    {
      path: '/map',
      redirect: '/admin/map'
    },
    {
      path: '/projects',
      redirect: '/admin/projects'
    },
    // 根路径
    {
      path: '/',
      redirect: '/login'
    },
    // 404
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  console.log('[路由] 导航:', to.path, '认证状态:', authStore.isLoggedIn)
  
  // 不需要认证的页面
  if (to.path === '/login') {
    if (authStore.isLoggedIn) {
      // 已登录，根据角色跳转
      if (authStore.isAdmin) {
        next('/admin/dashboard')
      } else {
        next('/user')
      }
    } else {
      next()
    }
    return
  }
  
  // 需要认证
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    console.log('[路由] 未登录，重定向到登录页')
    next('/login')
    return
  }
  
  // 检查角色权限
  const requiredRole = to.meta.requiredRole as RoleType | undefined
  if (requiredRole && !authStore.hasRole(requiredRole)) {
    console.log('[路由] 权限不足，重定向')
    if (authStore.isAdmin) {
      next('/admin/dashboard')
    } else {
      next('/user')
    }
    return
  }
  
  next()
})

export default router
