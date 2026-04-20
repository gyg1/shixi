<template>
  <header class="mars-header">
    <div class="logo-area">
      <el-icon class="logo-icon"><Promotion /></el-icon>
      <span class="system-name">三维城市规划协同管理系统</span>
    </div>

    <div class="nav-menu">
      <div
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeModule === item.key }"
        @click="emit('update:activeModule', item.key)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <div class="user-area">
      <el-dropdown @command="(command: string) => emit('command', command)">
        <div class="user-info">
          <el-avatar :size="32" icon="UserFilled" class="user-avatar" />
          <span class="user-name">{{ userName }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import {
  Promotion,
  HomeFilled,
  OfficeBuilding,
  Place,
  House,
  Warning,
  Money
} from '@element-plus/icons-vue'

type ModuleKey = 'home' | 'land' | 'project' | 'property' | 'danger' | 'fund'

defineProps<{
  activeModule: ModuleKey
  userName: string
}>()

const emit = defineEmits<{
  (e: 'update:activeModule', value: ModuleKey): void
  (e: 'command', value: string): void
}>()

const navItems: Array<{ key: ModuleKey; label: string; icon: any }> = [
  { key: 'home', label: '首页', icon: HomeFilled },
  { key: 'land', label: '土地储备', icon: Place },
  { key: 'project', label: '项目建设', icon: OfficeBuilding },
  { key: 'property', label: '房产租售', icon: House },
  { key: 'danger', label: '危险作业', icon: Warning },
  { key: 'fund', label: '资金信息', icon: Money }
]
</script>
