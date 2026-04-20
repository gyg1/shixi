<template>
  <div>
    <div class="mars-search-panel" v-show="activeModule === 'home'">
      <div class="search-box">
        <el-input
          :model-value="searchQuery"
          placeholder="搜索地点、项目、图层..."
          class="mars-input"
          @update:model-value="emit('update:searchQuery', String($event || ''))"
          @keyup.enter="emit('search')"
        >
          <template #append>
            <el-button @click="emit('search')">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="search-results" v-if="showResults">
        <div class="result-item" @click="emit('fly-center')">
          <el-icon><Location /></el-icon>
          <span>石家庄市中心</span>
        </div>
        <div class="result-item">
          <el-icon><Clock /></el-icon>
          <span>查询历史：裕华区</span>
        </div>
      </div>
    </div>

    <div class="mars-toolbar-top" v-show="activeModule === 'home'">
      <div class="tool-btn" :class="{ active: activePanel === 'basemap' }" @click="emit('toggle-panel', 'basemap')">
        <el-icon><MapLocation /></el-icon>
        <span>底图</span>
      </div>
      <div class="tool-btn" :class="{ active: activePanel === 'layers' }" @click="emit('toggle-panel', 'layers')">
        <el-icon><Files /></el-icon>
        <span>图层</span>
      </div>
      <div class="tool-btn" :class="{ active: activePanel === 'tools' }" @click="emit('toggle-panel', 'tools')">
        <el-icon><Tools /></el-icon>
        <span>工具</span>
      </div>
    </div>

    <div class="mars-panel right-panel" v-show="activePanel === 'basemap' && activeModule === 'home'">
      <div class="panel-header">
        <span>底图切换</span>
        <el-icon class="close-btn" @click="emit('close-panel')"><Close /></el-icon>
      </div>
      <div class="panel-content basemap-list">
        <div
          v-for="map in basemaps"
          :key="map.id"
          class="basemap-item"
          :class="{ active: currentBasemap === map.id }"
          @click="emit('change-basemap', map)"
        >
          <div class="map-icon" :style="{ background: map.icon }">
            <span class="basemap-emoji">{{ map.label }}</span>
          </div>
          <span class="map-name">{{ map.name }}</span>
        </div>

        <div class="control-group">
          <div class="control-item">
            <span>显示地形</span>
            <el-switch
              :model-value="showTerrain"
              @update:model-value="emit('update:showTerrain', Boolean($event))"
              @change="emit('toggle-terrain', Boolean($event))"
            />
          </div>
          <div class="control-item">
            <span>3D建筑（白模）</span>
            <el-switch
              :model-value="showBuildings"
              @update:model-value="emit('update:showBuildings', Boolean($event))"
              @change="emit('toggle-buildings', Boolean($event))"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="mars-panel right-panel" v-show="activePanel === 'layers' && activeModule === 'home'">
      <div class="panel-header">
        <span>图层管理</span>
        <el-icon class="close-btn" @click="emit('close-panel')"><Close /></el-icon>
      </div>
      <div class="panel-content">
        <el-tree
          :data="layerTreeData"
          show-checkbox
          node-key="id"
          :default-expanded-keys="['root', 'projects', 'land']"
          :default-checked-keys="checkedLayers"
          class="mars-tree"
          @check="handleLayerCheck"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
              <span>{{ node.label }}</span>
            </span>
          </template>
        </el-tree>

        <div class="opacity-control" v-if="selectedLayerId">
          <span class="label">透明度</span>
          <el-slider
            :model-value="layerOpacity"
            :step="0.1"
            :min="0"
            :max="1"
            size="small"
            @update:model-value="emit('update:layerOpacity', Number($event))"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search, Location, Clock, MapLocation, Files, Tools, Close } from '@element-plus/icons-vue'

type ModuleKey = 'home' | 'land' | 'project' | 'property' | 'danger' | 'fund'

defineProps<{
  searchQuery: string
  showResults: boolean
  activeModule: ModuleKey
  activePanel: string
  basemaps: Array<{ id: string; name: string; icon: string; label: string }>
  currentBasemap: string
  showTerrain: boolean
  showBuildings: boolean
  layerTreeData: any[]
  checkedLayers: string[]
  selectedLayerId: string
  layerOpacity: number
}>()

const emit = defineEmits<{
  (e: 'update:searchQuery', value: string): void
  (e: 'search'): void
  (e: 'fly-center'): void
  (e: 'toggle-panel', value: 'layers' | 'basemap' | 'tools'): void
  (e: 'close-panel'): void
  (e: 'change-basemap', value: any): void
  (e: 'update:showTerrain', value: boolean): void
  (e: 'toggle-terrain', value: boolean): void
  (e: 'update:showBuildings', value: boolean): void
  (e: 'toggle-buildings', value: boolean): void
  (e: 'layer-check', data: any, state: any): void
  (e: 'update:layerOpacity', value: number): void
}>()

const handleLayerCheck = (data: any, state: any) => {
  emit('layer-check', data, state)
}
</script>
