<template>
  <div class="project-form">
    <header class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1>{{ isEdit ? '编辑项目' : '新建项目' }}</h1>
    </header>

    <el-card class="form-card" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <!-- 第一步：选择大类 -->
        <el-form-item label="项目大类" prop="category" v-if="!isEdit">
          <el-select v-model="form.category" placeholder="请选择" style="width: 100%" @change="onCategoryChange">
            <el-option v-for="c in categories" :key="c.itemCode" :label="c.itemName" :value="c.itemCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目大类" v-if="isEdit">
          <el-tag :type="form.category === 'LAND' ? 'warning' : 'primary'">
            {{ form.category === 'LAND' ? '土地储备' : '工程项目' }}
          </el-tag>
        </el-form-item>

        <!-- 第二步：选择子类 -->
        <el-form-item label="项目子类" prop="subType" v-if="form.category">
          <el-select v-model="form.subType" placeholder="请选择子类" style="width: 100%" @change="onSubTypeChange" :disabled="isEdit">
            <el-option v-for="s in subTypes" :key="s.itemCode" :label="s.itemName" :value="s.itemCode" />
          </el-select>
        </el-form-item>

        <!-- 基础字段 -->
        <template v-if="form.subType">
          <el-divider content-position="left">基本信息</el-divider>

          <el-form-item label="项目编号" prop="projectCode">
            <el-input v-model="form.projectCode" placeholder="如 P2024001" />
          </el-form-item>
          
          <el-form-item label="项目名称" prop="projectName">
            <el-input v-model="form.projectName" placeholder="请输入项目名称" />
          </el-form-item>

          <el-form-item label="所属区县" prop="district">
            <el-select v-model="form.district" placeholder="请选择区县" style="width: 100%" clearable>
              <el-option label="长安区" value="长安区" />
              <el-option label="桥西区" value="桥西区" />
              <el-option label="新华区" value="新华区" />
              <el-option label="裕华区" value="裕华区" />
              <el-option label="井陉矿区" value="井陉矿区" />
              <el-option label="藁城区" value="藁城区" />
              <el-option label="鹿泉区" value="鹿泉区" />
              <el-option label="栾城区" value="栾城区" />
              <el-option label="正定县" value="正定县" />
              <el-option label="高新区" value="高新区" />
            </el-select>
          </el-form-item>

          <el-form-item label="项目状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
              <el-option label="待启动" value="PENDING" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="项目周期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="项目地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入地址" />
          </el-form-item>
          
          <el-form-item label="项目描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
          </el-form-item>

          <!-- 地图区域选择 -->
          <el-divider content-position="left">项目区域</el-divider>
          <el-form-item label="项目边界">
            <div class="geometry-selector">
              <el-button type="primary" @click="openMapDrawer">
                <el-icon><MapLocation /></el-icon>
                {{ form.geometry ? '重新选区' : '在地图上勾画区域' }}
              </el-button>
              <el-tag v-if="form.geometry" type="success" style="margin-left: 12px">
                ✓ 已选择区域
              </el-tag>
              <el-button v-if="form.geometry" text type="danger" @click="form.geometry = ''" style="margin-left: 8px">
                清除
              </el-button>
            </div>
          </el-form-item>

          <!-- 动态模板字段 -->
          <template v-if="templateFields.length > 0">
            <el-divider content-position="left">{{ templateName }} - 专属字段</el-divider>
            <el-form-item v-for="field in templateFields" :key="field.key" :label="field.label" :required="field.required">
              <template v-if="field.type === 'text'">
                <el-input v-model="extraData[field.key]" :placeholder="field.placeholder || `请输入${field.label}`">
                  <template #suffix v-if="field.unit">{{ field.unit }}</template>
                </el-input>
              </template>
              <template v-else-if="field.type === 'number'">
                <el-input-number v-model="extraData[field.key]" :placeholder="field.placeholder" style="width:100%" />
                <span v-if="field.unit" class="field-unit">{{ field.unit }}</span>
              </template>
              <template v-else-if="field.type === 'select'">
                <el-select v-model="extraData[field.key]" :placeholder="`请选择${field.label}`" style="width:100%">
                  <el-option v-for="opt in (field.options || [])" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </template>
              <template v-else-if="field.type === 'date'">
                <el-date-picker v-model="extraData[field.key]" type="date" :placeholder="`选择${field.label}`" style="width:100%" value-format="YYYY-MM-DD" />
              </template>
              <template v-else-if="field.type === 'textarea'">
                <el-input v-model="extraData[field.key]" type="textarea" :rows="3" :placeholder="`请输入${field.label}`" />
              </template>
            </el-form-item>
          </template>

          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">
              {{ isEdit ? '保存' : '创建' }}
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </template>
      </el-form>
    </el-card>

    <!-- 地图勾画对话框 -->
    <el-dialog v-model="mapDialogVisible" title="勾画项目区域" fullscreen destroy-on-close>
      <template #header>
        <div class="map-dialog-header">
          <span class="map-title">勾画项目区域</span>
          <div class="map-toolbar">
            <el-select v-model="selectedDistrict" placeholder="选择区县定位" size="small" clearable @change="onDistrictSelect" style="width: 140px">
              <el-option v-for="d in districtList" :key="d.name" :label="d.name" :value="d.name" />
            </el-select>
            <el-input v-model="placeSearchQuery" placeholder="搜索地名" size="small" style="width: 180px" @keyup.enter="searchPlace" clearable>
              <template #append>
                <el-button @click="searchPlace" size="small">搜索</el-button>
              </template>
            </el-input>
            <span class="draw-tip">左键添加点，右键完成</span>
            <el-button type="warning" size="small" @click="clearMapDraw">清除重画</el-button>
            <el-button type="success" size="small" @click="confirmMapDraw" :disabled="!tempGeometry">确认选区</el-button>
          </div>
        </div>
      </template>
      <div class="map-body">
        <div id="projectMapContainer" class="project-map-container"></div>
        <!-- 搜索结果列表 -->
        <div v-if="searchResults.length > 0" class="search-results-panel">
          <div class="search-result-item" v-for="(r, i) in searchResults" :key="i" @click="flyToSearchResult(r)">
            <div class="result-name">{{ r.name }}</div>
            <div class="result-addr">{{ r.address }}</div>
          </div>
        </div>
        <!-- 底图切换 -->
        <div class="basemap-toggle" @click="showBasemapPicker = !showBasemapPicker">
          🌐 底图
        </div>
        <div v-if="showBasemapPicker" class="basemap-picker">
          <div class="basemap-picker-title">底图切换</div>
          <div class="basemap-grid">
            <div v-for="m in projectBasemaps" :key="m.id" class="basemap-option" :class="{ active: currentProjectBasemap === m.id }" @click="changeProjectBasemap(m)">
              <div class="basemap-thumb" :style="{ background: m.icon }">
                <span class="basemap-emoji">{{ m.label }}</span>
              </div>
              <span>{{ m.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, reactive, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, MapLocation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { projectApi, dictApi, formTemplateApi, type Project, type SysDictItem, type TemplateField } from '@/api'
import * as Cesium from 'cesium'
import config from '@/config'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)
const projectId = computed(() => Number(route.params.id))

const dateRange = ref<string[]>([])
const categories = ref<SysDictItem[]>([])
const subTypes = ref<SysDictItem[]>([])
const templateFields = ref<TemplateField[]>([])
const templateName = ref('')
const extraData = reactive<Record<string, any>>({})

// 地图绘制
const mapDialogVisible = ref(false)
const selectedDistrict = ref('')
const placeSearchQuery = ref('')
const searchResults = ref<{name: string, address: string, lng: number, lat: number}[]>([])
const showBasemapPicker = ref(false)
const currentProjectBasemap = ref('arcgis_img')
const projectBasemaps = [
  { id: 'arcgis_img', name: 'ArcGIS影像', icon: 'linear-gradient(135deg, #1c3b57, #336699)', label: '🌍', type: 'arcgis' },
  { id: 'tianditu_img', name: '天地图影像', icon: 'linear-gradient(135deg, #2b4c3e, #4d8066)', label: '🛰️', type: 'tianditu_img' },
  { id: 'tianditu_vec', name: '天地图电子', icon: 'linear-gradient(135deg, #d3d9df, #e9ecef)', label: '🗺️', type: 'tianditu_vec' },
  { id: 'gaode_vec', name: '高德电子', icon: 'linear-gradient(135deg, #f0e6d2, #fff7e6)', label: '📍', type: 'gaode' },
  { id: 'dark', name: '黑色底图', icon: 'linear-gradient(135deg, #121212, #2c2c2c)', label: '🌙', type: 'dark' },
  { id: 'blue', name: '蓝色底图', icon: 'linear-gradient(135deg, #0f2027, #203a43)', label: '🌌', type: 'blue' },
]
let mapViewer: Cesium.Viewer | null = null
let mapDrawHandler: Cesium.ScreenSpaceEventHandler | null = null
let mapDrawPositions: Cesium.Cartesian3[] = []
let districtHighlightEntities: Cesium.Entity[] = []
let mapDrawEntities: Cesium.Entity[] = []
const tempGeometry = ref<string>('')

const form = ref<Project>({
  projectCode: '',
  projectName: '',
  projectType: '',
  category: '',
  subType: '',
  status: 'PENDING',
  startDate: '',
  endDate: '',
  address: '',
  district: '',
  description: ''
})

watch(dateRange, (val) => {
  if (val && val.length === 2) {
    form.value.startDate = val[0]
    form.value.endDate = val[1]
  } else {
    form.value.startDate = ''
    form.value.endDate = ''
  }
})

const rules: FormRules = {
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择项目大类', trigger: 'change' }],
  subType: [{ required: true, message: '请选择项目子类', trigger: 'change' }]
}

const goBack = () => router.push('/admin/projects')

const loadCategories = async () => {
  try {
    const res = await dictApi.listItemsByCode('project_category')
    if (res.code === 200) categories.value = res.data || []
  } catch (e) { console.error(e) }
}

const onCategoryChange = async (val: string) => {
  form.value.subType = ''
  subTypes.value = []
  templateFields.value = []
  const dictCode = val === 'LAND' ? 'land_sub_type' : 'engineering_sub_type'
  try {
    const res = await dictApi.listItemsByCode(dictCode)
    if (res.code === 200) subTypes.value = res.data || []
  } catch (e) { console.error(e) }
}

const onSubTypeChange = async (val: string) => {
  templateFields.value = []
  // 同步更新 projectType 为子类名称
  const matchSub = subTypes.value.find(s => s.itemCode === val)
  if (matchSub) form.value.projectType = matchSub.itemName
  
  try {
    const res = await formTemplateApi.match(form.value.category!, val)
    if (res.code === 200 && res.data) {
      templateName.value = res.data.templateName
      templateFields.value = JSON.parse(res.data.fields || '[]')
      // 初始化 extraData
      templateFields.value.forEach(f => {
        if (!(f.key in extraData)) extraData[f.key] = ''
      })
    }
  } catch (e) {
    console.log('该子类暂无模板配置')
  }
}

const loadProject = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const response = await projectApi.get(projectId.value)
    if (response.code === 200) {
      const p = response.data
      form.value = { ...p }
      if (p.startDate && p.endDate) {
        dateRange.value = [p.startDate, p.endDate]
      }
      // 加载子类列表和模板
      if (p.category) {
        await onCategoryChange(p.category)
        form.value.subType = p.subType || ''
        if (p.subType) await onSubTypeChange(p.subType)
      }
      // 恢复动态数据
      if (p.extraData) {
        try {
          const parsed = JSON.parse(p.extraData)
          Object.assign(extraData, parsed)
        } catch (e) {}
      }
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 将动态字段数据序列化
      form.value.extraData = JSON.stringify(extraData)
      
      const response = isEdit.value 
        ? await projectApi.update(projectId.value, form.value)
        : await projectApi.create(form.value)
      
      if (response.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        router.push('/admin/projects')
      } else {
        ElMessage.error(response.message)
      }
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  dateRange.value = []
}

// ========== 石家庄区县坐标 ==========
const districtList = [
  { name: '长安区', lng: 114.539, lat: 38.037, height: 15000 },
  { name: '桥西区', lng: 114.461, lat: 38.025, height: 15000 },
  { name: '新华区', lng: 114.463, lat: 38.051, height: 15000 },
  { name: '裕华区', lng: 114.531, lat: 38.006, height: 15000 },
  { name: '井陉矿区', lng: 114.065, lat: 38.065, height: 20000 },
  { name: '藁城区', lng: 114.847, lat: 38.022, height: 20000 },
  { name: '鹿泉区', lng: 114.313, lat: 38.086, height: 20000 },
  { name: '栾城区', lng: 114.648, lat: 37.900, height: 20000 },
  { name: '正定县', lng: 114.570, lat: 38.147, height: 20000 },
  { name: '高新区', lng: 114.570, lat: 38.020, height: 15000 },
  { name: '井陉县', lng: 114.145, lat: 38.032, height: 25000 },
  { name: '行唐县', lng: 114.553, lat: 38.438, height: 30000 },
  { name: '灵寿县', lng: 114.383, lat: 38.308, height: 30000 },
  { name: '赞皇县', lng: 114.386, lat: 37.665, height: 30000 },
  { name: '无极县', lng: 114.977, lat: 38.179, height: 25000 },
  { name: '平山县', lng: 114.186, lat: 38.260, height: 25000 },
  { name: '元氏县', lng: 114.525, lat: 37.766, height: 25000 },
  { name: '赵县', lng: 114.776, lat: 37.756, height: 25000 },
  { name: '晋州市', lng: 114.783, lat: 38.033, height: 25000 },
  { name: '新乐市', lng: 114.684, lat: 38.344, height: 25000 },
  { name: '辛集市', lng: 115.218, lat: 37.943, height: 25000 },
  { name: '深泽市', lng: 115.200, lat: 38.171, height: 25000 },
]

// ========== 地图勾画 ==========
const openMapDrawer = async () => {
  mapDialogVisible.value = true
  tempGeometry.value = ''
  selectedDistrict.value = ''
  placeSearchQuery.value = ''
  searchResults.value = []
  await nextTick()
  setTimeout(() => initProjectMap(), 200)
}

const initProjectMap = () => {
  Cesium.Ion.defaultAccessToken = config.cesiumToken
  mapViewer = new Cesium.Viewer('projectMapContainer', {
    animation: false, timeline: false, baseLayerPicker: false,
    geocoder: false, homeButton: false, sceneModePicker: false,
    navigationHelpButton: false, fullscreenButton: false,
    selectionIndicator: false, infoBox: false
  })
  
  // 石家庄全貌鸟瞄
  mapViewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(114.48, 38.03, 80000),
    orientation: { heading: 0, pitch: Cesium.Math.toRadians(-90), roll: 0 },
    duration: 1
  })
  
  // 加载默认底图
  const defaultMap = projectBasemaps.find(m => m.id === currentProjectBasemap.value) || projectBasemaps[0]
  if (defaultMap) {
    changeProjectBasemap(defaultMap)
  }
  
  startMapPolygonDraw()
}

// 区县选择 -> 飞到该区 + 高亮边界
const onDistrictSelect = async (name: string) => {
  if (!mapViewer || !name) return
  const d = districtList.find(x => x.name === name)
  if (!d) return
  
  // 清除上一次高亮
  districtHighlightEntities.forEach(e => mapViewer?.entities.remove(e))
  districtHighlightEntities = []
  
  // 飞到区县中心
  mapViewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(d.lng, d.lat, d.height),
    orientation: { heading: 0, pitch: Cesium.Math.toRadians(-90), roll: 0 },
    duration: 1.5
  })
  
  // 添加区县名称标注
  const label = mapViewer.entities.add({
    position: Cesium.Cartesian3.fromDegrees(d.lng, d.lat, 200),
    label: {
      text: d.name,
      font: 'bold 18px sans-serif',
      fillColor: Cesium.Color.YELLOW,
      outlineColor: Cesium.Color.BLACK,
      outlineWidth: 2,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      disableDepthTestDistance: Number.POSITIVE_INFINITY
    }
  })
  districtHighlightEntities.push(label)

  // 获取并绘制边界
  try {
    const keyword = encodeURIComponent("石家庄市" + d.name)
    const res = await fetch(`/api/map/administrative?keyword=${keyword}&searchWord=156130100&searchType=1`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token') || ''}` }
    })
    const data = await res.json()
    
    if (data.data && data.data.length > 0) {
       const boundaryInfo = data.data[0]
       if (boundaryInfo.points && boundaryInfo.points.length > 0) {
         // 解析多边形字符串 (格式为: {lon,lat;lon,lat;...}|{lon,lat;...})
         const polygons = boundaryInfo.points[0].region.split('|')
         
         polygons.forEach((polyStr: string) => {
           const coords = polyStr.split(',')
             .map((pointStr: string) => pointStr.trim().split(' ').map(Number))
             .filter((pair: number[]) => pair.length >= 2 && Number.isFinite(pair[0]) && Number.isFinite(pair[1]))
             .map((pair: number[]) => Cesium.Cartesian3.fromDegrees(pair[0]!, pair[1]!))
           
           if (coords.length > 2) {
             const polygonEntity = mapViewer!.entities.add({
               polygon: {
                 hierarchy: new Cesium.PolygonHierarchy(coords),
                 material: Cesium.Color.fromCssColorString('rgba(255, 0, 0, 0.15)'),
                 outline: true,
                 outlineColor: Cesium.Color.fromCssColorString('rgba(255, 0, 0, 0.8)'),
                 outlineWidth: 3,
                 heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
               }
             })
             districtHighlightEntities.push(polygonEntity)
           }
         })
       }
    }
  } catch (e) {
    console.warn('[区县边界] 获取/绘制失败', e)
  }
}

// 地名搜索 (通过后端代理，避免CORS)
const searchPlace = async () => {
  if (!placeSearchQuery.value.trim()) return
  searchResults.value = []
  try {
    const keyword = encodeURIComponent(placeSearchQuery.value)
    const res = await fetch(`/api/map/search?keyword=${keyword}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token') || ''}` }
    })
    const data = await res.json()
    if (data.pois && data.pois.length > 0) {
      searchResults.value = data.pois.map((p: any) => {
        const [lng, lat] = p.lonlat.split(',').map(Number)
        return { name: p.name, address: p.address || '', lng, lat }
      })
    } else {
      ElMessage.info('未找到相关地点')
    }
  } catch (e) {
    console.error('地名搜索失败', e)
    ElMessage.error('搜索失败，请重试')
  }
}

const flyToSearchResult = (r: { name: string, lng: number, lat: number }) => {
  if (!mapViewer) return
  searchResults.value = []
  mapViewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(r.lng, r.lat, 3000),
    orientation: { heading: 0, pitch: Cesium.Math.toRadians(-60), roll: 0 },
    duration: 1.5
  })
  // 添加定位标记
  const marker = mapViewer.entities.add({
    position: Cesium.Cartesian3.fromDegrees(r.lng, r.lat, 10),
    point: { pixelSize: 12, color: Cesium.Color.RED, outlineColor: Cesium.Color.WHITE, outlineWidth: 2 },
    label: {
      text: r.name,
      font: '14px sans-serif',
      fillColor: Cesium.Color.WHITE,
      outlineColor: Cesium.Color.BLACK,
      outlineWidth: 2,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      pixelOffset: new Cesium.Cartesian2(0, -16),
      disableDepthTestDistance: Number.POSITIVE_INFINITY
    }
  })
  districtHighlightEntities.push(marker)
}

const startMapPolygonDraw = () => {
  if (!mapViewer) return
  mapDrawPositions = []
  mapDrawEntities = []
  
  mapDrawHandler = new Cesium.ScreenSpaceEventHandler(mapViewer.scene.canvas)
  
  mapDrawHandler.setInputAction((click: any) => {
    const ray = mapViewer!.camera.getPickRay(click.position)
    if (!ray) return
    const cartesian = mapViewer!.scene.globe.pick(ray, mapViewer!.scene)
    if (!cartesian) return
    
    mapDrawPositions.push(cartesian)
    
    const pt = mapViewer!.entities.add({
      position: cartesian,
      point: { pixelSize: 8, color: Cesium.Color.ORANGE, outlineColor: Cesium.Color.WHITE, outlineWidth: 1 }
    })
    mapDrawEntities.push(pt)
    
    if (mapDrawPositions.length >= 2) {
      const line = mapViewer!.entities.add({
        polyline: { positions: mapDrawPositions.slice(-2), width: 2, material: Cesium.Color.ORANGE }
      })
      mapDrawEntities.push(line)
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
  
  mapDrawHandler.setInputAction(() => {
    if (mapDrawPositions.length < 3) {
      ElMessage.warning('请至少绘制3个点构成多边形')
      return
    }
    
    const firstPosition = mapDrawPositions[0]
    const lastPosition = mapDrawPositions[mapDrawPositions.length - 1]
    if (!firstPosition || !lastPosition) return

    const closeLine = mapViewer!.entities.add({
      polyline: { positions: [lastPosition, firstPosition], width: 2, material: Cesium.Color.ORANGE }
    })
    mapDrawEntities.push(closeLine)
    
    const poly = mapViewer!.entities.add({
      polygon: {
        hierarchy: new Cesium.PolygonHierarchy(mapDrawPositions),
        material: Cesium.Color.ORANGE.withAlpha(0.3),
        outline: true,
        outlineColor: Cesium.Color.ORANGE
      }
    })
    mapDrawEntities.push(poly)
    
    const coordinates: number[][] = mapDrawPositions.map(p => {
      const c = Cesium.Ellipsoid.WGS84.cartesianToCartographic(p)
      return [Cesium.Math.toDegrees(c.longitude), Cesium.Math.toDegrees(c.latitude)]
    })
    const firstCoordinate = coordinates[0]
    if (!firstCoordinate) return
    coordinates.push(firstCoordinate)
    tempGeometry.value = JSON.stringify({ type: 'Polygon', coordinates: [coordinates] })
    
    mapDrawHandler?.destroy()
    mapDrawHandler = null
    ElMessage.success('区域绘制完成，点击「确认选区」保存')
  }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
}

const clearMapDraw = () => {
  mapDrawEntities.forEach(e => mapViewer?.entities.remove(e))
  mapDrawEntities = []
  mapDrawPositions = []
  tempGeometry.value = ''
  mapDrawHandler?.destroy()
  mapDrawHandler = null
  startMapPolygonDraw()
}

const confirmMapDraw = () => {
  if (!tempGeometry.value) return
  form.value.geometry = tempGeometry.value
  mapDialogVisible.value = false
  mapViewer?.destroy()
  mapViewer = null
  ElMessage.success('项目区域已保存')
}

// ========== 底图切换 ==========
const addProjectTiandituLayer = (layer: string) => {
  const token = config.tiandituToken
  const url = `/tianditu/${layer}_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=${layer}&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${token}`
  mapViewer?.imageryLayers.addImageryProvider(
    new Cesium.UrlTemplateImageryProvider({ url, maximumLevel: 18 })
  )
}

const changeProjectBasemap = async (map: any) => {
  if (!mapViewer) return
  currentProjectBasemap.value = map.id
  showBasemapPicker.value = false
  mapViewer.imageryLayers.removeAll()
  
  try {
    switch (map.type) {
      case 'tianditu_img':
        addProjectTiandituLayer('img')
        addProjectTiandituLayer('cia')
        break
      case 'tianditu_vec':
        addProjectTiandituLayer('vec')
        addProjectTiandituLayer('cva')
        break
      case 'arcgis': {
        const provider = await Cesium.ArcGisMapServerImageryProvider.fromUrl(
          'https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer'
        )
        mapViewer.imageryLayers.addImageryProvider(provider)
        break
      }
      case 'gaode':
        mapViewer.imageryLayers.addImageryProvider(
          new Cesium.UrlTemplateImageryProvider({
            url: 'https://webrd02.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}',
            minimumLevel: 3, maximumLevel: 18
          })
        )
        break
      case 'dark':
        mapViewer.imageryLayers.addImageryProvider(
          new Cesium.UrlTemplateImageryProvider({
            url: 'https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
            credit: 'CartoDB'
          })
        )
        break
      case 'blue':
        mapViewer.imageryLayers.addImageryProvider(
          new Cesium.UrlTemplateImageryProvider({
            url: 'https://map.geoq.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer/tile/{z}/{y}/{x}',
            credit: 'GeoQ'
          })
        )
        break
    }
  } catch (e) {
    console.error('[底图] 切换失败', e)
    ElMessage.error('底图加载失败')
  }
}

onMounted(() => {
  loadCategories()
  loadProject()
})
</script>

<style scoped lang="scss">
.project-form {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  
  h1 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.form-card {
  max-width: 700px;
}

.field-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 13px;
}

.geometry-selector {
  display: flex;
  align-items: center;
}

.map-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: wrap;
  gap: 8px;

  .map-title {
    font-size: 16px;
    font-weight: 600;
  }

  .map-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .draw-tip {
    font-size: 12px;
    color: #909399;
  }
}

.map-body {
  position: relative;
  width: 100%;
  height: calc(100vh - 100px);
}

.project-map-container {
  width: 100%;
  height: 100%;
}

.search-results-panel {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 280px;
  max-height: 300px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 10;
}

.search-result-item {
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;

  &:hover {
    background: #ecf5ff;
  }

  &:last-child {
    border-bottom: none;
  }

  .result-name {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }

  .result-addr {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
  }
}

.basemap-toggle {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: rgba(23, 31, 46, 0.9);
  color: #fff;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  z-index: 10;
  border: 1px solid rgba(0, 180, 255, 0.3);
  transition: background 0.2s;

  &:hover {
    background: rgba(0, 180, 255, 0.3);
  }
}

.basemap-picker {
  position: absolute;
  bottom: 60px;
  right: 20px;
  width: 280px;
  background: rgba(18, 25, 38, 0.95);
  border: 1px solid rgba(0, 180, 255, 0.3);
  border-radius: 8px;
  padding: 12px;
  z-index: 10;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);

  .basemap-picker-title {
    color: #ccc;
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 10px;
  }

  .basemap-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .basemap-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    padding: 6px;
    border-radius: 6px;
    border: 2px solid transparent;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }

    &.active {
      border-color: #409eff;
      background: rgba(64, 158, 255, 0.15);
    }

    .basemap-thumb {
      width: 60px;
      height: 45px;
      border-radius: 4px;
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .basemap-emoji {
        font-size: 20px;
        filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
      }
    }

    span {
      font-size: 11px;
      color: #bbb;
    }
  }
}
</style>
