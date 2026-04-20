<template>
  <div class="user-management">
    <el-card shadow="never" class="filter-card">
      <div class="toolbar">
        <div class="filters">
          <el-input
            v-model="query.keyword"
            placeholder="搜索用户名 / 姓名 / 邮箱"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="query.role" placeholder="角色" clearable style="width: 160px" @change="handleSearch">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option v-if="authStore.isSuperAdmin" label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建用户
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="users" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEditDialog(row)" :disabled="!canManageTarget(row)">
              编辑
            </el-button>
            <el-button text type="primary" size="small" @click="openRoleDialog(row)" :disabled="!canManageTarget(row)">
              角色
            </el-button>
            <el-button text type="warning" size="small" @click="openPasswordDialog(row)" :disabled="!canManageTarget(row)">
              重置密码
            </el-button>
            <el-button text size="small" @click="handleToggleStatus(row)" :disabled="!canToggleTarget(row)">
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)" :disabled="!canDeleteTarget(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchUsers"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="userDialogVisible" :title="dialogMode === 'create' ? '新建用户' : '编辑用户'" width="520px">
      <el-form ref="userFormRef" :model="userForm" :rules="userFormRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option v-for="item in assignableRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="userForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitUserForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="修改角色" width="420px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ selectedUser?.username || '-' }}</span>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="roleForm.role" style="width: 100%">
            <el-option v-for="item in assignableRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRoleForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="420px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
        <el-form-item label="用户名">
          <span>{{ selectedUser?.username || '-' }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPasswordForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { userApi, type SystemUser } from '@/api'
import { useAuthStore, RoleType } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const users = ref<SystemUser[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  role: ''
})

const userDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const selectedUser = ref<SystemUser | null>(null)

const userFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const userForm = reactive<SystemUser>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  role: RoleType.USER,
  enabled: true
})

const roleForm = reactive({
  role: RoleType.USER
})

const passwordForm = reactive({
  password: ''
})

const roleOptions = [
  { label: '普通用户', value: RoleType.USER },
  { label: '管理员', value: RoleType.ADMIN },
  { label: '超级管理员', value: RoleType.SUPER_ADMIN }
]

const assignableRoleOptions = computed(() => {
  if (authStore.isSuperAdmin) return roleOptions
  return roleOptions.filter(item => item.value !== RoleType.SUPER_ADMIN)
})

const userFormRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordRules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于 6 位', trigger: 'blur' }
  ]
}

const getRoleText = (role?: string) => {
  const map: Record<string, string> = {
    USER: '普通用户',
    ADMIN: '管理员',
    SUPER_ADMIN: '超级管理员'
  }
  return role ? (map[role] || role) : '-'
}

const getRoleTagType = (role?: string) => {
  const map: Record<string, string> = {
    USER: 'info',
    ADMIN: 'warning',
    SUPER_ADMIN: 'danger'
  }
  return role ? (map[role] || 'info') : 'info'
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

const isSelf = (user: SystemUser) => user.id === authStore.user?.id
const isTargetSuperAdmin = (user: SystemUser) => user.role === RoleType.SUPER_ADMIN

const canManageTarget = (user: SystemUser) => {
  if (authStore.isSuperAdmin) return true
  return !isTargetSuperAdmin(user)
}

const canToggleTarget = (user: SystemUser) => canManageTarget(user) && !isSelf(user)
const canDeleteTarget = (user: SystemUser) => canManageTarget(user) && !isSelf(user)

const resetUserForm = () => {
  userForm.username = ''
  userForm.password = ''
  userForm.realName = ''
  userForm.email = ''
  userForm.phone = ''
  userForm.role = RoleType.USER
  userForm.enabled = true
  userForm.id = undefined
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.list({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      role: query.role || undefined
    })
    if (res.code === 200) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  fetchUsers()
}

const handleReset = () => {
  query.keyword = ''
  query.role = ''
  query.pageNum = 1
  fetchUsers()
}

const handleSizeChange = () => {
  query.pageNum = 1
  fetchUsers()
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  resetUserForm()
  userDialogVisible.value = true
}

const openEditDialog = (user: SystemUser) => {
  if (!canManageTarget(user)) {
    ElMessage.warning('当前账号无权编辑该用户')
    return
  }
  dialogMode.value = 'edit'
  resetUserForm()
  userForm.id = user.id
  userForm.username = user.username
  userForm.realName = user.realName || ''
  userForm.email = user.email || ''
  userForm.phone = user.phone || ''
  userForm.role = (user.role as RoleType) || RoleType.USER
  userForm.enabled = user.enabled ?? true
  userDialogVisible.value = true
}

const submitUserForm = async () => {
  const form = userFormRef.value
  if (!form) return
  await form.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload: SystemUser = {
        username: userForm.username,
        password: dialogMode.value === 'create' ? userForm.password : undefined,
        realName: userForm.realName || undefined,
        email: userForm.email || undefined,
        phone: userForm.phone || undefined,
        role: userForm.role,
        enabled: userForm.enabled
      }
      if (dialogMode.value === 'create') {
        await userApi.create(payload)
        ElMessage.success('用户创建成功')
      } else if (userForm.id) {
        await userApi.update(userForm.id, payload)
        await userApi.updateRole(userForm.id, userForm.role || RoleType.USER)
        ElMessage.success('用户更新成功')
      }
      userDialogVisible.value = false
      fetchUsers()
    } catch (error: any) {
      ElMessage.error(error.message || '保存用户失败')
    } finally {
      submitting.value = false
    }
  })
}

const openRoleDialog = (user: SystemUser) => {
  if (!canManageTarget(user)) {
    ElMessage.warning('当前账号无权修改该用户角色')
    return
  }
  selectedUser.value = user
  roleForm.role = (user.role as RoleType) || RoleType.USER
  roleDialogVisible.value = true
}

const submitRoleForm = async () => {
  if (!selectedUser.value?.id) return
  submitting.value = true
  try {
    await userApi.updateRole(selectedUser.value.id, roleForm.role)
    ElMessage.success('角色更新成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (error: any) {
    ElMessage.error(error.message || '角色更新失败')
  } finally {
    submitting.value = false
  }
}

const openPasswordDialog = (user: SystemUser) => {
  if (!canManageTarget(user)) {
    ElMessage.warning('当前账号无权重置该用户密码')
    return
  }
  selectedUser.value = user
  passwordForm.password = ''
  passwordDialogVisible.value = true
}

const submitPasswordForm = async () => {
  const form = passwordFormRef.value
  if (!form || !selectedUser.value?.id) return
  await form.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await userApi.resetPassword(selectedUser.value!.id!, passwordForm.password)
      ElMessage.success('密码重置成功')
      passwordDialogVisible.value = false
    } catch (error: any) {
      ElMessage.error(error.message || '密码重置失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleToggleStatus = async (user: SystemUser) => {
  if (isSelf(user)) {
    ElMessage.warning('不能禁用当前登录账号')
    return
  }
  if (!canManageTarget(user)) {
    ElMessage.warning('当前账号无权修改该用户状态')
    return
  }
  const actionText = user.enabled ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户“${user.username}”吗？`, '提示', { type: 'warning' })
    await userApi.toggleStatus(user.id!)
    ElMessage.success(`用户已${actionText}`)
    fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${actionText}用户失败`)
    }
  }
}

const handleDelete = async (user: SystemUser) => {
  if (isSelf(user)) {
    ElMessage.warning('不能删除当前登录账号')
    return
  }
  if (!canManageTarget(user)) {
    ElMessage.warning('当前账号无权删除该用户')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除用户“${user.username}”吗？`, '警告', { type: 'warning' })
    await userApi.delete(user.id!)
    ElMessage.success('用户删除成功')
    if (users.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1
    }
    fetchUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除用户失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped lang="scss">
.user-management {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .filters {
    width: 100%;
  }
}
</style>
