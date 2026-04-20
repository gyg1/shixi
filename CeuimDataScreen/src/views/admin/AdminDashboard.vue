<template>
  <div class="admin-dashboard">
    <div class="stats-row">
      <el-card class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </el-card>
    </div>
    
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>最近项目</template>
          <el-table :data="recentProjects" size="small">
            <el-table-column prop="projectName" label="项目名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="projectType" label="类型" width="80" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/projects/create')">新建项目</el-button>
            <el-button @click="$router.push('/admin/map')">查看地图</el-button>
            <el-button @click="$router.push('/admin/statistics')">数据统计</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { projectApi } from '@/api'

const stats = ref([
  { label: '总项目数', value: 0 },
  { label: '进行中', value: 0 },
  { label: '已完成', value: 0 }
])

const recentProjects = ref<any[]>([])

const getStatusType = (status: string) => {
  const map: Record<string, string> = { PENDING: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { PENDING: '待启动', IN_PROGRESS: '进行中', COMPLETED: '已完成' }
  return map[status] || status
}

onMounted(async () => {
  try {
    const statsRes = await projectApi.statistics()
    if (statsRes.code === 200) {
      stats.value = [
        { label: '总项目数', value: statsRes.data.total || 0 },
        { label: '进行中', value: statsRes.data.inProgress || 0 },
        { label: '已完成', value: statsRes.data.completed || 0 }
      ]
    }
    
    const listRes = await projectApi.list({ pageNum: 1, pageSize: 5 })
    if (listRes.code === 200) {
      recentProjects.value = listRes.data.records
    }
  } catch (e) {
    console.error('[仪表板] 加载失败', e)
  }
})
</script>

<style scoped lang="scss">
.admin-dashboard {
  padding: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  
  .stat-value {
    font-size: 32px;
    font-weight: 700;
    color: #409eff;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  
  .el-button {
    width: 100%;
  }
}
</style>
