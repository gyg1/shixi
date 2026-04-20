<template>
  <div class="dict-management">
    <el-row :gutter="16">
      <!-- 左侧: 字典列表 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" size="small" @click="showDictDialog()">
                <el-icon><Plus /></el-icon> 新增
              </el-button>
            </div>
          </template>
          <div v-loading="loadingDicts">
            <div
              v-for="dict in dicts"
              :key="dict.id"
              class="dict-item"
              :class="{ active: currentDict?.id === dict.id }"
              @click="selectDict(dict)"
            >
              <div class="dict-info">
                <div class="dict-name">{{ dict.dictName }}</div>
                <div class="dict-code">{{ dict.dictCode }}</div>
              </div>
              <div class="dict-actions">
                <el-button text size="small" @click.stop="showDictDialog(dict)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button text size="small" type="danger" @click.stop="deleteDict(dict)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-empty v-if="dicts.length === 0" description="暂无字典" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧: 字典项列表 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ currentDict ? `${currentDict.dictName} - 字典项` : '请选择字典' }}</span>
              <el-button
                type="primary"
                size="small"
                :disabled="!currentDict"
                @click="showItemDialog()"
              >
                <el-icon><Plus /></el-icon> 新增字典项
              </el-button>
            </div>
          </template>
          <el-table :data="items" v-loading="loadingItems" border stripe>
            <el-table-column prop="itemCode" label="编码" width="140" />
            <el-table-column prop="itemName" label="名称" width="140" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="颜色" width="100">
              <template #default="{ row }">
                <div v-if="row.color" class="color-tag">
                  <span class="color-dot" :style="{ background: row.color }"></span>
                  {{ row.color }}
                </div>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="showItemDialog(row)">编辑</el-button>
                <el-button text type="danger" size="small" @click="deleteItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!currentDict" description="请先选择左侧字典类型" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型对话框 -->
    <el-dialog v-model="dictDialogVisible" :title="editingDict ? '编辑字典' : '新增字典'" width="450px">
      <el-form :model="dictForm" label-width="80px">
        <el-form-item label="字典编码" required>
          <el-input v-model="dictForm.dictCode" placeholder="如 project_category" :disabled="!!editingDict" />
        </el-form-item>
        <el-form-item label="字典名称" required>
          <el-input v-model="dictForm.dictName" placeholder="如 项目大类" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dictForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDict" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典项对话框 -->
    <el-dialog v-model="itemDialogVisible" :title="editingItem ? '编辑字典项' : '新增字典项'" width="450px">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="编码" required>
          <el-input v-model="itemForm.itemCode" placeholder="如 LAND" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="itemForm.itemName" placeholder="如 土地储备" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="itemForm.color" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="itemForm.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dictApi, type SysDict, type SysDictItem } from '@/api'

const loadingDicts = ref(false)
const loadingItems = ref(false)
const saving = ref(false)
const dicts = ref<SysDict[]>([])
const items = ref<SysDictItem[]>([])
const currentDict = ref<SysDict | null>(null)

// 字典对话框
const dictDialogVisible = ref(false)
const editingDict = ref<SysDict | null>(null)
const dictForm = ref<SysDict>({ dictCode: '', dictName: '', description: '' })

// 字典项对话框
const itemDialogVisible = ref(false)
const editingItem = ref<SysDictItem | null>(null)
const itemForm = ref<SysDictItem>({ itemCode: '', itemName: '', sort: 0, color: '', status: 'ACTIVE' })

const loadDicts = async () => {
  loadingDicts.value = true
  try {
    const res = await dictApi.listDicts()
    if (res.code === 200) dicts.value = res.data
  } catch (e) {
    console.error('加载字典失败', e)
  } finally {
    loadingDicts.value = false
  }
}

const selectDict = async (dict: SysDict) => {
  currentDict.value = dict
  loadingItems.value = true
  try {
    const res = await dictApi.listItems(dict.id!)
    if (res.code === 200) items.value = res.data
  } catch (e) {
    console.error('加载字典项失败', e)
  } finally {
    loadingItems.value = false
  }
}

const showDictDialog = (dict?: SysDict) => {
  editingDict.value = dict || null
  dictForm.value = dict
    ? { ...dict }
    : { dictCode: '', dictName: '', description: '' }
  dictDialogVisible.value = true
}

const saveDict = async () => {
  if (!dictForm.value.dictCode || !dictForm.value.dictName) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    if (editingDict.value) {
      await dictApi.updateDict(editingDict.value.id!, dictForm.value)
    } else {
      await dictApi.createDict(dictForm.value)
    }
    ElMessage.success(editingDict.value ? '更新成功' : '创建成功')
    dictDialogVisible.value = false
    loadDicts()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const deleteDict = (dict: SysDict) => {
  ElMessageBox.confirm(`确定删除字典「${dict.dictName}」？该操作会同时删除所有字典项`, '警告', { type: 'warning' })
    .then(async () => {
      await dictApi.deleteDict(dict.id!)
      ElMessage.success('删除成功')
      if (currentDict.value?.id === dict.id) {
        currentDict.value = null
        items.value = []
      }
      loadDicts()
    })
    .catch(() => {})
}

const showItemDialog = (item?: SysDictItem) => {
  editingItem.value = item || null
  itemForm.value = item
    ? { ...item }
    : { itemCode: '', itemName: '', sort: 0, color: '', status: 'ACTIVE' }
  itemDialogVisible.value = true
}

const saveItem = async () => {
  if (!itemForm.value.itemCode || !itemForm.value.itemName) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    if (editingItem.value) {
      await dictApi.updateItem(editingItem.value.id!, itemForm.value)
    } else {
      await dictApi.createItem(currentDict.value!.id!, itemForm.value)
    }
    ElMessage.success(editingItem.value ? '更新成功' : '创建成功')
    itemDialogVisible.value = false
    selectDict(currentDict.value!)
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const deleteItem = (item: SysDictItem) => {
  ElMessageBox.confirm(`确定删除「${item.itemName}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await dictApi.deleteItem(item.id!)
      ElMessage.success('删除成功')
      selectDict(currentDict.value!)
    })
    .catch(() => {})
}

onMounted(() => loadDicts())
</script>

<style scoped lang="scss">
.dict-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dict-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .dict-info {
    .dict-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
    .dict-code {
      font-size: 12px;
      color: #909399;
      margin-top: 2px;
    }
  }
}

.color-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;

  .color-dot {
    width: 14px;
    height: 14px;
    border-radius: 3px;
    border: 1px solid #ddd;
  }
}

.text-muted {
  color: #c0c4cc;
}
</style>
