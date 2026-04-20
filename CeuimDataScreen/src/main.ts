import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'

import App from './App.vue'
import router from './router'
import { config } from './config'
import { useAuthStore } from './stores/auth'

// 设置 Cesium Ion Token
Cesium.Ion.defaultAccessToken = config.cesiumToken

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

// 初始化认证状态
const authStore = useAuthStore()
authStore.initializeAuth()

app.mount('#app')
