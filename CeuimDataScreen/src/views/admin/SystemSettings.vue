<template>
  <div class="system-settings">
    <header class="page-header">
      <h1>系统设置</h1>
    </header>

    <el-card class="setting-card">
      <template #header>基本设置</template>
      <el-form label-width="100px">
        <el-form-item label="系统名称">
          <el-input v-model="settings.systemName" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="setting-card">
      <template #header>地图设置</template>
      <el-form label-width="100px">
        <el-form-item label="默认中心点">
          <el-input v-model="settings.defaultCenter" placeholder="经度,纬度" />
        </el-form-item>
        <el-form-item label="缩放级别">
          <el-input-number v-model="settings.defaultZoom" :min="1" :max="18" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="setting-card">
      <template #header>安全设置</template>
      <el-form label-width="100px">
        <el-form-item label="Token有效期">
          <el-input-number v-model="settings.tokenExpiry" :min="1" :max="168" />
          <span class="unit">小时</span>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="actions">
      <el-button type="primary" @click="saveSettings">保存</el-button>
      <el-button @click="resetSettings">重置</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const settings = ref({
  systemName: '三维城市规划协同管理系统',
  defaultCenter: '114.48,38.03',
  defaultZoom: 10,
  tokenExpiry: 24
})

const saveSettings = () => ElMessage.success('保存成功')
const resetSettings = () => {
  settings.value = {
    systemName: '三维城市规划协同管理系统',
    defaultCenter: '114.48,38.03',
    defaultZoom: 10,
    tokenExpiry: 24
  }
  ElMessage.info('已重置')
}
</script>

<style scoped lang="scss">
.system-settings {
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

.setting-card {
  margin-bottom: 16px;
  max-width: 600px;
  
  .unit {
    margin-left: 8px;
    color: #909399;
  }
}

.actions {
  margin-top: 24px;
}
</style>
