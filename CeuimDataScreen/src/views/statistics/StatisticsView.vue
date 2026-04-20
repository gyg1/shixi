<template>
  <div class="statistics-view">
    <header class="page-header">
      <h1>数据统计</h1>
    </header>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card" v-for="stat in statCards" :key="stat.label">
        <div class="stat-number">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card class="chart-card">
        <template #header>项目类型分布</template>
        <div class="chart-placeholder">
          <el-empty description="ECharts 图表待集成" />
        </div>
      </el-card>
      <el-card class="chart-card">
        <template #header>月度项目趋势</template>
        <div class="chart-placeholder">
          <el-empty description="ECharts 图表待集成" />
        </div>
      </el-card>
    </div>

    <el-card class="chart-card full">
      <template #header>各区域项目分布</template>
      <div class="chart-placeholder">
        <el-empty description="ECharts 图表待集成" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const statCards = ref([
  { label: '总项目数', value: 156 },
  { label: '进行中', value: 89 },
  { label: '已完成', value: 67 },
  { label: '土地储备', value: 7 }
])

onMounted(() => {
  console.log('[统计页面] 组件挂载')
})
</script>

<style scoped lang="scss">
.statistics-view {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 16px;
  
  h1 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  
  .stat-number {
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

.charts-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  &.full {
    grid-column: 1 / -1;
  }
  
  .chart-placeholder {
    height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
