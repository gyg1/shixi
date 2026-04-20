<template>
  <div class="map-view">
    <!-- 顶部导航栏 -->
    <header class="map-header">
      <div class="header-left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <span class="title">三维地图</span>
      </div>
      <div class="header-right">
        <el-button size="small" @click="flyToShijiazhuang">定位石家庄</el-button>
        <span class="user-info">{{ authStore.user?.username }}</span>
      </div>
    </header>

    <!-- 地图容器 -->
    <div class="map-container">
      <div id="cesiumContainer" ref="cesiumContainer"></div>
      
      <!-- 图层控制面板 -->
      <div class="layer-panel">
        <h3>图层控制</h3>
        <div class="layer-list">
          <div v-for="layer in layers" :key="layer.id" class="layer-item">
            <el-checkbox v-model="layer.visible" @change="toggleLayer(layer)">
              {{ layer.name }}
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 信息面板 -->
      <div v-if="selectedEntity" class="info-panel">
        <div class="info-header">
          <span>{{ selectedEntity.name || '详细信息' }}</span>
          <el-button text @click="selectedEntity = null">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="info-content">
          <div v-for="(value, key) in selectedEntity.properties" :key="key" class="info-item">
            <span class="label">{{ key }}:</span>
            <span class="value">{{ value }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Close } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { config } from '@/config'
import * as Cesium from 'cesium'

const router = useRouter()
const authStore = useAuthStore()

const cesiumContainer = ref<HTMLElement | null>(null)
let viewer: Cesium.Viewer | null = null

const selectedEntity = ref<any>(null)
const layers = reactive(config.layers.map(layer => ({ ...layer })))

const goBack = () => router.push('/dashboard')

// 石家庄坐标
const SHIJIAZHUANG = {
  longitude: 114.48,
  latitude: 38.03,
  height: 50000  // 50km 高度俯瞰
}

const flyToShijiazhuang = () => {
  if (!viewer) return
  console.log('[地图] 飞行到石家庄')
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(
      SHIJIAZHUANG.longitude,
      SHIJIAZHUANG.latitude,
      SHIJIAZHUANG.height
    ),
    duration: 2
  })
}

const initCesium = () => {
  console.log('[地图] 初始化 Cesium')
  Cesium.Ion.defaultAccessToken = config.cesiumToken

  viewer = new Cesium.Viewer('cesiumContainer', {
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    navigationHelpButton: false,
    fullscreenButton: false,
    selectionIndicator: true,
    infoBox: false
    // 不指定 imageryProvider，使用 Cesium Ion 默认底图
  })

  // 初始定位到石家庄
  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(
      SHIJIAZHUANG.longitude,
      SHIJIAZHUANG.latitude,
      SHIJIAZHUANG.height
    )
  })

  console.log('[地图] 相机定位到石家庄:', SHIJIAZHUANG)

  loadGeoJSONLayers()
  setupClickHandler()
}

const loadGeoJSONLayers = async () => {
  if (!viewer) return
  console.log('[地图] 开始加载 GeoJSON 图层')

  for (const layer of layers) {
    try {
      console.log('[地图] 加载图层:', layer.name, layer.file)
      const dataSource = await Cesium.GeoJsonDataSource.load(`/${layer.file}`, {
        stroke: Cesium.Color.fromCssColorString('#409eff'),
        fill: Cesium.Color.fromCssColorString('rgba(64, 158, 255, 0.3)'),
        strokeWidth: 2
      })
      
      dataSource.name = layer.id
      dataSource.show = layer.visible
      viewer.dataSources.add(dataSource)
      console.log('[地图] 图层加载成功:', layer.name)
    } catch (error) {
      console.error('[地图] 图层加载失败:', layer.name, error)
    }
  }
}

const toggleLayer = (layer: any) => {
  if (!viewer) return
  const dataSource = viewer.dataSources.getByName(layer.id)[0]
  if (dataSource) {
    dataSource.show = layer.visible
    console.log('[地图] 切换图层:', layer.name, layer.visible)
  }
}

const setupClickHandler = () => {
  if (!viewer) return

  const handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  
  handler.setInputAction((movement: any) => {
    const pickedObject = viewer?.scene.pick(movement.position)
    
    if (Cesium.defined(pickedObject) && pickedObject.id) {
      const entity = pickedObject.id
      const properties: Record<string, any> = {}
      
      if (entity.properties) {
        const propertyNames = entity.properties.propertyNames
        for (const name of propertyNames) {
          properties[name] = entity.properties[name]?.getValue()
        }
      }
      
      selectedEntity.value = {
        name: entity.name || properties.name || '未命名',
        properties
      }
      console.log('[地图] 选中实体:', selectedEntity.value)
    } else {
      selectedEntity.value = null
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
}

onMounted(() => initCesium())

onUnmounted(() => {
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
})
</script>

<style scoped lang="scss">
.map-view {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.map-header {
  height: 50px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .user-info {
    color: #606266;
    font-size: 14px;
  }
}

.map-container {
  flex: 1;
  position: relative;
  
  #cesiumContainer {
    width: 100%;
    height: 100%;
  }
}

.layer-panel {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 180px;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  h3 {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
    padding-bottom: 8px;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .layer-item {
    margin-bottom: 6px;
    
    :deep(.el-checkbox__label) {
      font-size: 13px;
    }
  }
}

.info-panel {
  position: absolute;
  bottom: 16px;
  left: 16px;
  width: 280px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .info-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border-bottom: 1px solid #e4e7ed;
    font-weight: 600;
    color: #303133;
  }
  
  .info-content {
    padding: 12px;
    max-height: 180px;
    overflow-y: auto;
    
    .info-item {
      margin-bottom: 6px;
      display: flex;
      font-size: 13px;
      
      .label {
        color: #909399;
        min-width: 60px;
      }
      
      .value {
        color: #303133;
      }
    }
  }
}
</style>
