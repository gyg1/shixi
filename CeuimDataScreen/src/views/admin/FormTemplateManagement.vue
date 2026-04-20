<template>
  <div class="template-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>表单模板管理</span>
          <el-button type="primary" size="small" @click="showDialog()">
            <el-icon><Plus /></el-icon> 新增模板
          </el-button>
        </div>
      </template>

      <el-table :data="templates" v-loading="loading" border stripe>
        <el-table-column prop="templateName" label="模板名称" width="160" />
        <el-table-column label="大类" width="120">
          <template #default="{ row }">
            <el-tag :type="row.category === 'LAND' ? 'warning' : 'primary'" size="small">
              {{ row.category === 'LAND' ? '土地储备' : '工程项目' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subType" label="子类" width="120" />
        <el-table-column label="字段数" width="80">
          <template #default="{ row }">
            {{ parseFields(row.fields).length }}
          </template>
        </el-table-column>
        <el-table-column label="字段预览" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="f in parseFields(row.fields).slice(0, 4)" :key="f.key" size="small" style="margin: 2px">
              {{ f.label }}
            </el-tag>
            <el-tag v-if="parseFields(row.fields).length > 4" size="small" type="info" style="margin: 2px">
              +{{ parseFields(row.fields).length - 4 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="showDialog(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="deleteTemplate(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 模板编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑模板' : '新增模板'" width="700px" top="5vh">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="模板名称" required>
              <el-input v-model="form.templateName" placeholder="如 居住用地项目模板" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="大类" required>
              <el-select v-model="form.category" @change="onCategoryChange" style="width:100%">
                <el-option v-for="c in categories" :key="c.itemCode" :label="c.itemName" :value="c.itemCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子类" required>
              <el-select v-model="form.subType" style="width:100%">
                <el-option v-for="s in subTypes" :key="s.itemCode" :label="s.itemName" :value="s.itemCode" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">表单字段配置</el-divider>

        <div class="field-list">
          <div v-for="(field, index) in fields" :key="index" class="field-row">
            <el-input v-model="field.key" placeholder="字段key" style="width:100px" size="small" />
            <el-input v-model="field.label" placeholder="显示名" style="width:100px" size="small" />
            <el-select v-model="field.type" style="width:100px" size="small">
              <el-option label="文本" value="text" />
              <el-option label="数字" value="number" />
              <el-option label="下拉" value="select" />
              <el-option label="日期" value="date" />
              <el-option label="多行" value="textarea" />
            </el-select>
            <el-input v-model="field.unit" placeholder="单位" style="width:60px" size="small" />
            <el-checkbox v-model="field.required" size="small">必填</el-checkbox>
            <el-button type="danger" text size="small" @click="fields.splice(index, 1)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>

        <el-button type="primary" text @click="addField" style="margin-top: 8px">
          <el-icon><Plus /></el-icon> 添加字段
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formTemplateApi, dictApi, type FormTemplate, type TemplateField, type SysDictItem } from '@/api'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editing = ref<FormTemplate | null>(null)
const templates = ref<FormTemplate[]>([])
const categories = ref<SysDictItem[]>([])
const subTypes = ref<SysDictItem[]>([])

const form = ref({ templateName: '', category: '', subType: '' })
const fields = ref<TemplateField[]>([])

const parseFields = (fieldsStr: string): TemplateField[] => {
  try { return JSON.parse(fieldsStr) } catch { return [] }
}

const loadTemplates = async () => {
  loading.value = true
  try {
    const res = await formTemplateApi.list()
    if (res.code === 200) templates.value = res.data || []
  } catch (e) {
    console.error('加载模板失败', e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await dictApi.listItemsByCode('project_category')
    if (res.code === 200) categories.value = res.data || []
  } catch (e) { console.error(e) }
}

const onCategoryChange = async (val: string) => {
  form.value.subType = ''
  subTypes.value = []
  const dictCode = val === 'LAND' ? 'land_sub_type' : 'engineering_sub_type'
  try {
    const res = await dictApi.listItemsByCode(dictCode)
    if (res.code === 200) subTypes.value = res.data || []
  } catch (e) { console.error(e) }
}

const showDialog = async (t?: FormTemplate) => {
  editing.value = t || null
  if (t) {
    form.value = { templateName: t.templateName, category: t.category, subType: t.subType }
    fields.value = parseFields(t.fields)
    await onCategoryChange(t.category)
    form.value.subType = t.subType
  } else {
    form.value = { templateName: '', category: '', subType: '' }
    fields.value = []
  }
  dialogVisible.value = true
}

const addField = () => {
  fields.value.push({ key: '', label: '', type: 'text', required: false, unit: '' })
}

const save = async () => {
  if (!form.value.templateName || !form.value.category || !form.value.subType) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    const data: FormTemplate = {
      templateName: form.value.templateName,
      category: form.value.category,
      subType: form.value.subType,
      fields: JSON.stringify(fields.value)
    }
    if (editing.value) {
      await formTemplateApi.update(editing.value.id!, data)
    } else {
      await formTemplateApi.create(data)
    }
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadTemplates()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const deleteTemplate = (t: FormTemplate) => {
  ElMessageBox.confirm(`确定删除模板「${t.templateName}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await formTemplateApi.delete(t.id!)
      ElMessage.success('删除成功')
      loadTemplates()
    })
    .catch(() => {})
}

onMounted(() => {
  loadTemplates()
  loadCategories()
})
</script>

<style scoped lang="scss">
.template-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-list {
  max-height: 300px;
  overflow-y: auto;
}

.field-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 6px;
}
</style>
