<template>
  <div class="danger-work-list">
    <header class="page-header">
      <h1>危险作业历史记录</h1>
    </header>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索申报编号/作业名称"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
        style="width: 240px"
      />
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <el-table :data="pagedList" style="width: 100%" v-loading="loading" border>
      <el-table-column prop="workCode" label="申报编号" width="160" />
      <el-table-column prop="districtName" label="所属片区" width="140" />
      <el-table-column prop="workType" label="作业类型" width="120" />
      <el-table-column prop="workName" label="作业名称" min-width="150" />
      <el-table-column prop="riskLevel" label="风险等级" width="110" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.riskLevel === '重大风险' ? 'danger' : row.riskLevel === '较大风险' ? 'warning' : 'success'"
            size="small"
          >
            {{ row.riskLevel || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applicant" label="申请人" width="100" />
      <el-table-column prop="responsiblePerson" label="负责人" width="100" />
      <el-table-column label="计划时间" width="220" align="center">
        <template #default="{ row }">
          {{ formatDate(row.planStartTime) }} ~ {{ formatDate(row.planEndTime) || '未设置' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredList.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { dangerousWorkApi, type DangerousWork } from '@/api'

const loading = ref(false)
const listData = ref<DangerousWork[]>([])

const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return status ? (map[status] || status) : '-'
}

const getStatusTagType = (status?: string) => {
  const map: Record<string, string> = {
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return status ? (map[status] || 'info') : 'info'
}

const formatDate = (value?: string) => {
  return value ? value.slice(0, 10) : ''
}

const formatDateTime = (value?: string) => {
  return value ? value.slice(0, 16).replace('T', ' ') : '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await dangerousWorkApi.list()
    if (res.code === 200) {
      listData.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取危险作业列表失败')
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  return listData.value.filter(item => {
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      const matchName = (item.workName || '').toLowerCase().includes(keyword)
      const matchCode = (item.workCode || '').toLowerCase().includes(keyword)
      if (!matchName && !matchCode) return false
    }

    if (filterStatus.value && item.status !== filterStatus.value) {
      return false
    }

    return true
  })
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

const handleSearch = () => {
  currentPage.value = 1
}

const handlePageChange = () => {
  // currentPage is synced by v-model
}

const handleSizeChange = () => {
  currentPage.value = 1
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.danger-work-list {
  padding: 24px;
  background-color: #fff;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.search-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
