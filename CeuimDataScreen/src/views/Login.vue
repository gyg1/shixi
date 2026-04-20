<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 左侧信息 -->
      <div class="info-panel">
        <h1>三维城市规划协同管理系统</h1>
        <p>基于GIS的智能项目进度管理平台</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Location /></el-icon>
            <span>GIS地图展示</span>
          </div>
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>实时数据统计</span>
          </div>
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>项目协同管理</span>
          </div>
        </div>
      </div>
      
      <!-- 右侧表单 -->
      <div class="form-panel">
        <div class="form-container">
          <h2>{{ isLogin ? '登录' : '注册' }}</h2>
          
          <el-form ref="formRef" :model="formData" :rules="formRules" @submit.prevent="handleSubmit">
            <el-form-item prop="username">
              <el-input
                v-model="formData.username"
                placeholder="用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="email" v-if="!isLogin">
              <el-input
                v-model="formData.email"
                placeholder="邮箱"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword" v-if="!isLogin">
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-button type="primary" size="large" :loading="loading" @click="handleSubmit" style="width: 100%">
              {{ isLogin ? '登录' : '注册' }}
            </el-button>
          </el-form>
          
          <div class="form-footer">
            <span>{{ isLogin ? '没有账户？' : '已有账户？' }}</span>
            <a @click="toggleMode">{{ isLogin ? '立即注册' : '立即登录' }}</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock, Message, Location, DataAnalysis, Document } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isLogin = ref(true)
const loading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const formRules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3-20字符', trigger: 'blur' }
  ],
  email: !isLogin.value ? [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ] : [],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: !isLogin.value ? [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== formData.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ] : []
}))

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formData.username = ''
  formData.email = ''
  formData.password = ''
  formData.confirmPassword = ''
  formRef.value?.clearValidate()
}

const handleSubmit = () => {
  if (!formRef.value) return
  
  formRef.value.validate()
    .then(valid => {
      if (!valid) return
      
      loading.value = true
      console.log('[登录页] 提交表单:', formData.username)
      
      if (isLogin.value) {
        return authStore.login({
          username: formData.username,
          password: formData.password
        })
      } else {
        return authStore.register({
          username: formData.username,
          email: formData.email,
          password: formData.password
        })
      }
    })
    .then(result => {
      if (!result) return
      
      if (result.success) {
        ElMessage.success(result.message)
        if (isLogin.value) {
          console.log('[登录页] 登录成功，跳转到仪表板')
          router.push('/dashboard')
        } else {
          isLogin.value = true
          toggleMode()
        }
      } else {
        console.log('[登录页] 操作失败:', result.message)
        ElMessage.error(result.message)
      }
    })
    .catch((error: any) => {
      console.error('[登录页] 错误:', error)
      ElMessage.error('操作失败')
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.info-panel {
  flex: 1;
  padding: 60px 40px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 12px 0;
  }
  
  p {
    font-size: 14px;
    opacity: 0.9;
    margin: 0 0 40px 0;
  }
  
  .features {
    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      font-size: 14px;
      
      .el-icon {
        font-size: 20px;
      }
    }
  }
}

.form-panel {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 320px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 32px 0;
    text-align: center;
  }
}

.el-form-item {
  margin-bottom: 20px;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;
  
  a {
    color: #409eff;
    cursor: pointer;
    margin-left: 4px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 768px) {
  .login-box {
    flex-direction: column;
  }
  
  .info-panel {
    padding: 40px 30px;
  }
  
  .form-panel {
    padding: 40px 30px;
  }
}
</style>