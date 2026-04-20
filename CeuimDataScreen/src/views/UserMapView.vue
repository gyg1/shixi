<template>
  <div class="user-map-view">
    <!-- 全屏地图容器 -->
    <div id="cesiumContainer" class="cesium-container"></div>

    <!-- 1. 顶部导航栏 -->
    <header class="mars-header">
      <div class="logo-area">
        <el-icon class="logo-icon"><Promotion /></el-icon>
        <span class="system-name">三维城市规划协同管理系统</span>
      </div>
      
      <div class="nav-menu">
        <div class="nav-item" :class="{ active: activeModule === 'home' }" @click="activeModule = 'home'">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </div>
        <div class="nav-item" :class="{ active: activeModule === 'land' }" @click="activeModule = 'land'">
          <el-icon><Place /></el-icon>
          <span>土地储备</span>
        </div>
        <div class="nav-item" :class="{ active: activeModule === 'project' }" @click="activeModule = 'project'">
          <el-icon><OfficeBuilding /></el-icon>
          <span>项目建设</span>
        </div>
        <div class="nav-item" :class="{ active: activeModule === 'property' }" @click="activeModule = 'property'">
          <el-icon><House /></el-icon>
          <span>房产租售</span>
        </div>
        <div class="nav-item" :class="{ active: activeModule === 'danger' }" @click="activeModule = 'danger'">
          <el-icon><Warning /></el-icon>
          <span>危险作业</span>
        </div>
        <div class="nav-item" :class="{ active: activeModule === 'fund' }" @click="activeModule = 'fund'">
          <el-icon><Money /></el-icon>
          <span>资金信息</span>
        </div>
      </div>
      
      <div class="user-area">
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" icon="UserFilled" class="user-avatar" />
            <span class="user-name">{{ authStore.user?.realName || authStore.user?.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 2. 左上角搜索栏 -->
    <div class="mars-search-panel" v-show="activeModule === 'home'">
      <div class="search-box">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索地点、项目、图层..." 
          class="mars-input"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <!-- 搜索历史/结果面板 (模拟) -->
      <div class="search-results" v-if="showResults">
        <div class="result-item" @click="flyToShijiazhuang">
          <el-icon><Location /></el-icon>
          <span>石家庄市中心</span>
        </div>
        <div class="result-item">
          <el-icon><Clock /></el-icon>
          <span>查询历史: 裕华区</span>
        </div>
      </div>
    </div>

    <!-- 3. 右上角工具条 -->
    <div class="mars-toolbar-top" v-show="activeModule === 'home'">
      <div class="tool-btn" :class="{ active: activePanel === 'basemap' }" @click="togglePanel('basemap')">
        <el-icon><MapLocation /></el-icon>
        <span>底图</span>
      </div>
      <div class="tool-btn" :class="{ active: activePanel === 'layers' }" @click="togglePanel('layers')">
        <el-icon><Files /></el-icon>
        <span>图层</span>
      </div>
      <div class="tool-btn" :class="{ active: activePanel === 'tools' }" @click="togglePanel('tools')">
        <el-icon><Tools /></el-icon>
        <span>工具</span>
      </div>
    </div>

    <!-- 4. 右侧面板 (整合底图与图层) -->
    <div class="mars-panel right-panel" v-show="activePanel === 'basemap' && activeModule === 'home'">
      <div class="panel-header">
        <span>底图切换</span>
        <el-icon class="close-btn" @click="activePanel = null"><Close /></el-icon>
      </div>
      <div class="panel-content basemap-list">
        <div 
          v-for="map in basemaps" 
          :key="map.id" 
          class="basemap-item" 
          :class="{ active: currentBasemap === map.id }"
          @click="changeBasemap(map)"
        >
          <div class="map-icon" :style="{ background: map.icon }">
            <span class="basemap-emoji">{{ map.label }}</span>
          </div>
          <span class="map-name">{{ map.name }}</span>
        </div>
        
        <div class="control-group">
          <div class="control-item">
            <span>显示地形</span>
            <el-switch v-model="showTerrain" @change="toggleTerrain" />
          </div>
          <div class="control-item">
            <span>3D建筑 (白模)</span>
            <el-switch v-model="showBuildings" @change="toggleBuildings" />
          </div>
        </div>
      </div>
    </div>

    <!-- 图层管理面板 -->
    <div class="mars-panel right-panel" v-show="activePanel === 'layers' && activeModule === 'home'">
      <div class="panel-header">
        <span>图层管理</span>
        <el-icon class="close-btn" @click="activePanel = null"><Close /></el-icon>
      </div>
      <div class="panel-content">
        <el-tree
          :data="layerTreeData"
          show-checkbox
          node-key="id"
          :default-expanded-keys="['root', 'projects', 'land']"
          :default-checked-keys="checkedLayers"
          @check-change="handleLayerCheck"
          class="mars-tree"
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
          <el-slider v-model="layerOpacity" :step="0.1" :min="0" :max="1" size="small" />
        </div>
      </div>
    </div>
    
    <!-- 工具面板 -->
    <div class="mars-panel right-panel tools-panel" v-show="activePanel === 'tools' && activeModule === 'home'">
      <div class="panel-header">工具箱 <el-icon class="close-btn" @click="activePanel = null"><Close /></el-icon></div>
      <div class="panel-content">
        <!-- 工具图标网格 -->
        <div class="tool-grid">
          <div class="tool-item" :class="{ active: activeTool === 'measure' }" @click="setActiveTool('measure')">
            <el-icon><Aim /></el-icon>
            <span>图上量算</span>
          </div>
          <div class="tool-item" :class="{ active: activeTool === 'locate' }" @click="setActiveTool('locate')">
            <el-icon><Position /></el-icon>
            <span>坐标定位</span>
          </div>
          <div class="tool-item" :class="{ active: activeTool === 'bookmark' }" @click="setActiveTool('bookmark')">
            <el-icon><Collection /></el-icon>
            <span>视角书签</span>
          </div>
          <div class="tool-item" :class="{ active: activeTool === 'marker' }" @click="setActiveTool('marker')">
            <el-icon><Flag /></el-icon>
            <span>我的标记</span>
          </div>
          <div class="tool-item" :class="{ active: activeTool === 'spatial' }" @click="setActiveTool('spatial')">
            <el-icon><DataAnalysis /></el-icon>
            <span>空间分析</span>
          </div>
          <div class="tool-item" :class="{ active: activeTool === 'draw' }" @click="setActiveTool('draw')">
            <el-icon><Edit /></el-icon>
            <span>图上标绘</span>
          </div>
        </div>
        
        <!-- 量算工具子面板 -->
        <div v-if="activeTool === 'measure'" class="tool-subpanel">
          <div class="sub-header">测量工具</div>
          <div class="btn-group">
            <el-button size="small" :type="measureMode === 'distance' ? 'primary' : 'default'" @click="startMeasure('distance')">
              <el-icon><Share /></el-icon> 测距
            </el-button>
            <el-button size="small" :type="measureMode === 'area' ? 'primary' : 'default'" @click="startMeasure('area')">
              <el-icon><Grid /></el-icon> 测面
            </el-button>
            <el-button size="small" type="danger" @click="clearMeasure">
              <el-icon><Delete /></el-icon> 清除
            </el-button>
          </div>
          <div class="measure-result" v-if="measureResult">
            <div class="result-label">测量结果:</div>
            <div class="result-value">{{ measureResult }}</div>
          </div>
          <div class="tool-tip" v-if="measureMode">
            {{ measureMode === 'distance' ? '左键点击添加点，右键结束' : '左键点击添加顶点，右键闭合' }}
          </div>
        </div>
        
        <!-- 坐标定位子面板 -->
        <div v-if="activeTool === 'locate'" class="tool-subpanel">
          <div class="sub-header">坐标定位</div>
          <el-form label-width="60px" size="small">
            <el-form-item label="经度">
              <el-input-number v-model="locateForm.longitude" :precision="6" :step="0.01" :min="-180" :max="180" />
            </el-form-item>
            <el-form-item label="纬度">
              <el-input-number v-model="locateForm.latitude" :precision="6" :step="0.01" :min="-90" :max="90" />
            </el-form-item>
            <el-form-item label="高度">
              <el-input-number v-model="locateForm.height" :precision="0" :step="100" :min="100" :max="100000" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="flyToCoordinate">
                <el-icon><Promotion /></el-icon> 飞行定位
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 视角书签子面板 -->
        <div v-if="activeTool === 'bookmark'" class="tool-subpanel">
          <div class="sub-header">
            视角书签
            <el-button size="small" type="primary" @click="saveBookmark">
              <el-icon><Plus /></el-icon> 保存当前
            </el-button>
          </div>
          <div class="bookmark-list" v-if="bookmarks.length > 0">
            <div v-for="(bm, idx) in bookmarks" :key="idx" class="bookmark-item" @click="flyToBookmark(bm)">
              <div class="bm-info">
                <div class="bm-name">{{ bm.name }}</div>
                <div class="bm-coords">{{ bm.longitude.toFixed(4) }}, {{ bm.latitude.toFixed(4) }}</div>
              </div>
              <el-icon class="bm-delete" @click.stop="deleteBookmark(idx)"><Delete /></el-icon>
            </div>
          </div>
          <el-empty v-else description="暂无书签" :image-size="40" />
        </div>
        
        <!-- 我的标记子面板 -->
        <div v-if="activeTool === 'marker'" class="tool-subpanel">
          <div class="sub-header">
            我的标记
            <el-button size="small" :type="isAddingMarker ? 'warning' : 'primary'" @click="toggleMarkerMode">
              <el-icon><Plus /></el-icon> {{ isAddingMarker ? '取消添加' : '添加标记' }}
            </el-button>
          </div>
          <div class="tool-tip" v-if="isAddingMarker">点击地图添加标记点</div>
          <div class="marker-list" v-if="markers.length > 0">
            <div v-for="(mk, idx) in markers" :key="idx" class="marker-item" @click="flyToMarker(mk)">
              <div class="mk-info">
                <div class="mk-name">{{ mk.name }}</div>
                <div class="mk-desc" v-if="mk.description">{{ mk.description }}</div>
              </div>
              <el-icon class="mk-delete" @click.stop="deleteMarker(idx)"><Delete /></el-icon>
            </div>
          </div>
          <el-empty v-else description="暂无标记" :image-size="40" />
        </div>
        
        <!-- 空间分析子面板 -->
        <div v-if="activeTool === 'spatial'" class="tool-subpanel">
          <div class="sub-header">空间分析</div>
          <div class="btn-group vertical">
            <el-button size="small" :type="spatialMode === 'buffer' ? 'primary' : 'default'" @click="startSpatialAnalysis('buffer')">
              <el-icon><CircleCheck /></el-icon> 缓冲区分析
            </el-button>
            <el-button size="small" :type="spatialMode === 'query' ? 'primary' : 'default'" @click="startSpatialAnalysis('query')">
              <el-icon><Search /></el-icon> 点击查询
            </el-button>
            <el-button size="small" :type="spatialMode === 'statistics' ? 'primary' : 'default'" @click="startSpatialAnalysis('statistics')">
              <el-icon><PieChart /></el-icon> 区域统计
            </el-button>
            <el-button size="small" type="danger" @click="clearSpatialAnalysis">
              <el-icon><Delete /></el-icon> 清除结果
            </el-button>
          </div>
          
          <!-- 缓冲区参数 -->
          <div v-if="spatialMode === 'buffer'" class="spatial-params">
            <el-form label-width="70px" size="small">
              <el-form-item label="缓冲半径">
                <el-input-number v-model="bufferRadius" :min="100" :max="50000" :step="100" />
                <span class="unit">米</span>
              </el-form-item>
            </el-form>
            <div class="tool-tip">点击地图选择中心点，自动生成缓冲区</div>
          </div>
          
          <!-- 点击查询提示 -->
          <div v-if="spatialMode === 'query'" class="tool-tip">
            点击地图上的要素查看详细属性
          </div>
          
          <!-- 区域统计提示 -->
          <div v-if="spatialMode === 'statistics'" class="tool-tip">
            左键点击绘制多边形范围，右键结束绘制
          </div>
          
          <!-- 分析结果 -->
          <div v-if="spatialResult" class="spatial-result">
            <div class="result-header">
              <span>分析结果</span>
              <el-icon class="close-btn" @click="spatialResult = null"><Close /></el-icon>
            </div>
            <div class="result-body">
              <div v-if="spatialResult.type === 'buffer'">
                <p><strong>缓冲中心:</strong> {{ spatialResult.center }}</p>
                <p><strong>缓冲半径:</strong> {{ spatialResult.radius }} 米</p>
                <p><strong>范围内要素:</strong> {{ spatialResult.count }} 个</p>
                <div v-if="spatialResult.features?.length" class="feature-list">
                  <div v-for="(f, i) in spatialResult.features" :key="i" class="feature-item">
                    {{ f.name || f.id || `要素 ${i + 1}` }}
                  </div>
                </div>
              </div>
              <div v-if="spatialResult.type === 'query'">
                <p><strong>要素名称:</strong> {{ spatialResult.name || '未命名' }}</p>
                <div v-for="(value, key) in spatialResult.properties" :key="key" class="prop-row">
                  <span class="prop-key">{{ key }}:</span>
                  <span class="prop-val">{{ value }}</span>
                </div>
              </div>
              <div v-if="spatialResult.type === 'statistics'">
                <p><strong>统计区域面积:</strong> {{ spatialResult.area }}</p>
                <p><strong>包含要素数:</strong> {{ spatialResult.count }} 个</p>
                <div class="stat-breakdown" v-if="spatialResult.breakdown">
                  <div v-for="(count, layer) in spatialResult.breakdown" :key="layer" class="stat-item">
                    <span>{{ layer }}:</span>
                    <span>{{ count }} 个</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 图上标绘子面板 -->
        <div v-if="activeTool === 'draw'" class="tool-subpanel">
          <div class="sub-header">图上标绘</div>
          <div class="btn-group">
            <el-button size="small" :type="drawMode === 'point' ? 'primary' : 'default'" @click="startDrawing('point')">
              <el-icon><Location /></el-icon> 画点
            </el-button>
            <el-button size="small" :type="drawMode === 'polyline' ? 'primary' : 'default'" @click="startDrawing('polyline')">
              <el-icon><Share /></el-icon> 画线
            </el-button>
            <el-button size="small" :type="drawMode === 'polygon' ? 'primary' : 'default'" @click="startDrawing('polygon')">
              <el-icon><Grid /></el-icon> 画面
            </el-button>
          </div>
          <div class="tool-tip" v-if="drawMode">
            {{ drawMode === 'point' ? '点击地图添加点' : drawMode === 'polyline' ? '左键添加点，右键结束' : '左键添加顶点，右键闭合多边形' }}
          </div>
          
          <div class="sub-header" style="margin-top: 15px;">
            已保存标绘
            <el-button size="small" @click="loadSavedDrawings">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
          <div class="drawing-list" v-if="savedDrawings.length > 0">
            <div v-for="d in savedDrawings" :key="d.id" class="drawing-item" @click="flyToDrawing(d)">
              <div class="d-info">
                <div class="d-name">{{ d.name }}</div>
                <div class="d-meta">
                  <el-tag size="small" :type="getCategoryTagType(d.category)">{{ getCategoryLabel(d.category) }}</el-tag>
                  <span>{{ d.drawType }}</span>
                </div>
              </div>
              <el-icon class="d-delete" @click.stop="deleteDrawing(d.id)"><Delete /></el-icon>
            </div>
          </div>
          <el-empty v-else description="暂无标绘数据" :image-size="40" />
        </div>
      </div>
    </div>
    
    <!-- 保存标绘对话框 -->
    <el-dialog v-model="drawingDialogVisible" title="保存标绘" width="400px" :close-on-click-modal="false">
      <el-form :model="newDrawing" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="newDrawing.name" placeholder="请输入标绘名称" />
        </el-form-item>
        <el-form-item label="业务分类" required>
          <el-select v-model="newDrawing.category" placeholder="选择分类" style="width: 100%">
            <el-option label="项目建设" value="PROJECT" />
            <el-option label="土地储备" value="LAND" />
            <el-option label="房产租售" value="PROPERTY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newDrawing.description" type="textarea" :rows="2" placeholder="可选描述" />
        </el-form-item>
        <el-form-item label="测量信息" v-if="newDrawing.area || newDrawing.length">
          <div v-if="newDrawing.area">面积: {{ newDrawing.area }}</div>
          <div v-if="newDrawing.length">长度: {{ newDrawing.length }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDrawing">取消</el-button>
        <el-button type="primary" @click="saveDrawing" :loading="drawingSaving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 添加标记对话框 -->
    <el-dialog v-model="markerDialogVisible" title="添加标记" width="360px" :close-on-click-modal="false">
      <el-form :model="newMarker" label-width="60px">
        <el-form-item label="名称">
          <el-input v-model="newMarker.name" placeholder="请输入标记名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newMarker.description" type="textarea" :rows="2" placeholder="可选描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="markerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddMarker">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 保存书签对话框 -->
    <el-dialog v-model="bookmarkDialogVisible" title="保存视角书签" width="360px" :close-on-click-modal="false">
      <el-form :model="newBookmark" label-width="60px">
        <el-form-item label="名称">
          <el-input v-model="newBookmark.name" placeholder="请输入书签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookmarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveBookmark">确定</el-button>
      </template>
    </el-dialog>

    <!-- 5. 左下角工具栏 -->
    <div class="mars-toolbar-side" v-show="activeModule === 'home'">
      <el-tooltip content="复位" placement="right">
        <div class="side-btn" @click="resetCamera">
          <el-icon><Refresh /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="回到主视图" placement="right">
        <div class="side-btn" @click="flyToShijiazhuang">
          <el-icon><HomeFilled /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="放大" placement="right">
        <div class="side-btn" @click="zoomIn">
          <el-icon><Plus /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="缩小" placement="right">
        <div class="side-btn" @click="zoomOut">
          <el-icon><Minus /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="全屏" placement="right">
        <div class="side-btn" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
        </div>
      </el-tooltip>
    </div>

    <!-- 6. 底部状态栏 -->
    <footer class="mars-statusbar">
      <div class="status-item">视角高度: {{ cameraInfo.height }}米</div>
      <div class="status-item">经度: {{ cameraInfo.longitude }}</div>
      <div class="status-item">纬度: {{ cameraInfo.latitude }}</div>
      <div class="status-item">俯仰角: {{ cameraInfo.pitch }}°</div>
      <div class="status-copyright">© 2024 三维城市规划系统</div>
    </footer>

    <!-- 7. 信息弹窗 -->
    <div v-if="selectedEntity" class="mars-popup" :style="{ left: popupPosition.x + 'px', top: popupPosition.y + 'px' }">
      <div class="popup-header">
        <span>{{ selectedEntity.name }}</span>
        <el-icon class="close-btn" @click="selectedEntity = null"><Close /></el-icon>
      </div>
      <div class="popup-content">
        <div v-for="(value, key) in selectedEntity.properties" :key="key" class="prop-item">
          <span class="prop-label">{{ key }}:</span>
          <span class="prop-value">{{ value }}</span>
        </div>
      </div>
    </div>

    <!-- 项目详情浮层 -->
    <div v-if="projectDetailVisible" class="project-detail-panel">
      <div class="detail-header">
        <h3>{{ projectDetail?.projectName }}</h3>
        <el-icon class="close-btn" @click="projectDetailVisible = false"><Close /></el-icon>
      </div>
      <div class="detail-body" v-if="projectDetail">
        <div class="detail-row">
          <span class="detail-label">编号</span>
          <span>{{ projectDetail.projectCode }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">大类</span>
          <el-tag size="small" :type="projectDetail.category === 'LAND' ? 'warning' : 'primary'">
            {{ projectDetail.category === 'LAND' ? '土地储备' : '工程项目' }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">子类</span>
          <span>{{ projectDetail.projectType }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态</span>
          <el-tag size="small" :type="{PENDING:'info',IN_PROGRESS:'warning',COMPLETED:'success',CANCELLED:'danger'}[projectDetail.status] || 'info'">
            {{ {PENDING:'待启动',IN_PROGRESS:'进行中',COMPLETED:'已完成',CANCELLED:'已取消'}[projectDetail.status] || projectDetail.status }}
          </el-tag>
        </div>
        <div class="detail-row" v-if="projectDetail.district">
          <span class="detail-label">区县</span>
          <span>{{ projectDetail.district }}</span>
        </div>
        <div class="detail-row" v-if="projectDetail.totalInvestment != null">
          <span class="detail-label">总投资</span>
          <span>{{ projectDetail.totalInvestment }} 万元</span>
        </div>
        <div class="detail-row" v-if="projectDetail.startDate">
          <span class="detail-label">周期</span>
          <span>{{ projectDetail.startDate }} ~ {{ projectDetail.endDate }}</span>
        </div>
        <div class="detail-row" v-if="projectDetail.address">
          <span class="detail-label">地址</span>
          <span>{{ projectDetail.address }}</span>
        </div>
        <div class="detail-row" v-if="projectDetail.description">
          <span class="detail-label">描述</span>
          <span>{{ projectDetail.description }}</span>
        </div>

        <el-divider />
        <div class="detail-section-title">
          <span>附件列表</span>
          <el-upload v-if="authStore.isAdmin" :show-file-list="false" :http-request="uploadProjectFile" style="display:inline-block;margin-left:8px">
            <el-button size="small" type="primary">上传文件</el-button>
          </el-upload>
        </div>
        <div v-if="projectFiles.length === 0" class="no-files">暂无附件</div>
        <div v-for="f in projectFiles" :key="f.id" class="file-item-new">
          <div class="file-info-main">
            <el-icon class="file-type-icon"><component :is="getFileIcon(f.fileName)" /></el-icon>
            <div class="file-text">
              <span class="file-name-new" :title="f.fileName">{{ f.fileName }}</span>
              <span class="file-size-new">{{ ((f.fileSize || 0) / 1024).toFixed(1) }}KB</span>
            </div>
          </div>
          <div class="file-actions-new">
            <el-tooltip content="预览" placement="top">
              <el-button circle size="small" type="primary" :icon="View" @click="openFilePreview(f)" />
            </el-tooltip>
            <el-tooltip content="下载" placement="top">
              <a :href="getFileDownloadUrl(f.id!)" target="_blank" class="icon-link-btn">
                <el-button circle size="small" type="success" :icon="Download" />
              </a>
            </el-tooltip>
            <el-tooltip v-if="authStore.isAdmin" content="删除" placement="top">
              <el-button circle size="small" type="danger" :icon="Delete" @click="deleteProjectFile(f.id!)" />
            </el-tooltip>
          </div>
        </div>

        <div v-if="authStore.isAdmin" class="detail-actions">
          <el-button type="primary" @click="editProjectFromMap">编辑项目</el-button>
        </div>
      </div>
    </div>
    
    <!-- 项目内联编辑弹窗 -->
    <el-dialog v-model="projectEditVisible" title="编辑项目" width="520px" class="project-edit-dialog" append-to-body destroy-on-close>
      <el-form :model="projectEditForm" label-width="80px" label-position="left">
        <el-form-item label="项目名称">
          <el-input v-model="projectEditForm.projectName" />
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input v-model="projectEditForm.projectCode" />
        </el-form-item>
        <el-form-item label="大类">
          <el-select v-model="projectEditForm.category" style="width:100%" popper-class="dark-popper">
            <el-option label="土地储备" value="LAND" />
            <el-option label="工程项目" value="ENGINEERING" />
          </el-select>
        </el-form-item>
        <el-form-item label="子类">
          <el-select v-model="projectEditForm.subType" style="width:100%" filterable popper-class="dark-popper">
            <el-option 
              v-for="s in (projectEditForm.category === 'LAND' ? landSubTypes : subTypes)" 
              :key="s.itemCode" 
              :label="s.itemName" 
              :value="s.itemCode" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="projectEditForm.status" style="width:100%" popper-class="dark-popper">
            <el-option label="待启动" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="区县">
          <el-select v-model="projectEditForm.district" style="width:100%" filterable placeholder="请选择区县" popper-class="dark-popper">
            <el-option value="长安区" label="长安区" />
            <el-option value="桥西区" label="桥西区" />
            <el-option value="新华区" label="新华区" />
            <el-option value="裕华区" label="裕华区" />
            <el-option value="井陉矿区" label="井陉矿区" />
            <el-option value="藁城区" label="藁城区" />
            <el-option value="鹿泉区" label="鹿泉区" />
            <el-option value="栾城区" label="栾城区" />
            <el-option value="井陉县" label="井陉县" />
            <el-option value="正定县" label="正定县" />
            <el-option value="行唐县" label="行唐县" />
            <el-option value="灵寿县" label="灵寿县" />
            <el-option value="高邑县" label="高邑县" />
            <el-option value="深泽县" label="深泽县" />
            <el-option value="赞皇县" label="赞皇县" />
            <el-option value="无极县" label="无极县" />
            <el-option value="平山县" label="平山县" />
            <el-option value="元氏县" label="元氏县" />
            <el-option value="赵县" label="赵县" />
            <el-option value="辛集市" label="辛集市" />
            <el-option value="晋州市" label="晋州市" />
            <el-option value="新乐市" label="新乐市" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(亩)">
          <el-input-number v-model="projectEditForm.area" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="总投资(万)">
          <el-input-number v-model="projectEditForm.totalInvestment" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="projectEditForm.startDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:100%" popper-class="dark-popper" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="projectEditForm.endDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:100%" popper-class="dark-popper" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="projectEditForm.address" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="projectEditForm.description" type="textarea" :rows="3" />
        </el-form-item>

        <!-- 工程项目专属字段 -->
        <template v-if="projectEditForm.category === 'ENGINEERING'">
          <el-form-item label="二级集团">
            <el-select v-model="projectEditForm.secondaryGroup" placeholder="请选择二级集团" filterable clearable style="width:100%" popper-class="dark-popper">
              <el-option v-for="cg in companyGroups" :key="cg.itemCode" :label="cg.itemName" :value="cg.itemName" />
            </el-select>
          </el-form-item>
          <el-form-item label="施工单位">
            <el-select v-model="projectEditForm.constructionUnit" placeholder="请选择施工单位" filterable clearable style="width:100%" popper-class="dark-popper">
              <el-option v-for="cg in companyGroups" :key="cg.itemCode" :label="cg.itemName" :value="cg.itemName" />
            </el-select>
          </el-form-item>
          <el-form-item label="建设单位">
            <el-select v-model="projectEditForm.developmentUnit" placeholder="请选择建设单位" filterable clearable style="width:100%" popper-class="dark-popper">
              <el-option v-for="cg in companyGroups" :key="cg.itemCode" :label="cg.itemName" :value="cg.itemName" />
            </el-select>
          </el-form-item>
          <el-form-item label="资金来源">
            <el-input v-model="projectEditForm.fundSource" placeholder="资金来源描述" />
          </el-form-item>
          <el-form-item label="当前投资">
            <el-input-number v-model="projectEditForm.currentInvestment" :min="0" style="width:100%" placeholder="万元" />
          </el-form-item>
          <el-form-item label="手续办理">
            <el-upload :http-request="(opts) => handleSpecialFileUpload('proceduresStatus', opts)" :show-file-list="false" accept=".pdf">
              <el-button type="primary" size="small">上传 PDF 手续文件</el-button>
            </el-upload>
            <div v-if="projectEditForm.proceduresStatus" style="margin-top: 5px; width: 100%; word-break: break-all;">
              <span v-for="(url, idx) in projectEditForm.proceduresStatus.split(',').filter(Boolean)" :key="idx" style="margin-right: 10px;">
                <a :href="url" target="_blank" style="color: #409EFF; text-decoration: none;">📄 文件{{ idx + 1 }}</a>
                <el-icon @click="removeSpecialFile('proceduresStatus', idx)" style="color: #F56C6C; cursor: pointer; margin-left: 5px;"><Close /></el-icon>
              </span>
            </div>
          </el-form-item>
          <el-form-item label="竣工手续">
            <el-upload :http-request="(opts) => handleSpecialFileUpload('completionProcedures', opts)" :show-file-list="false" accept=".pdf">
              <el-button type="primary" size="small">上传 PDF 竣工文件</el-button>
            </el-upload>
            <div v-if="projectEditForm.completionProcedures" style="margin-top: 5px; width: 100%; word-break: break-all;">
              <span v-for="(url, idx) in projectEditForm.completionProcedures.split(',').filter(Boolean)" :key="idx" style="margin-right: 10px;">
                <a :href="url" target="_blank" style="color: #409EFF; text-decoration: none;">📄 文件{{ idx + 1 }}</a>
                <el-icon @click="removeSpecialFile('completionProcedures', idx)" style="color: #F56C6C; cursor: pointer; margin-left: 5px;"><Close /></el-icon>
              </span>
            </div>
          </el-form-item>
          <el-form-item label="存在问题">
            <el-input v-model="projectEditForm.existingProblems" type="textarea" :rows="2" placeholder="目前存在的问题" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="projectEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProjectEdit" :loading="projectEditSaving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 片区详情弹窗（图三效果） -->
    <div v-if="districtDetailVisible" class="district-detail-panel">
      <div class="district-detail-header">
        <h3 :style="{ color: districtDetail?.color || '#00d4ff' }">{{ districtDetail?.projectName }}</h3>
        <el-icon class="close-btn" @click="districtDetailVisible = false"><Close /></el-icon>
      </div>
      <div class="district-detail-body" v-if="districtDetail">
        <div class="district-map-preview">
          <div class="map-placeholder" :style="{ background: districtExtraData.planningImageUrl ? `url(${districtExtraData.planningImageUrl}) center/cover no-repeat` : `linear-gradient(135deg, ${districtDetail.color || '#0a4d8c'}33, ${districtDetail.color || '#0a4d8c'}11)` }">
            <span class="map-label" v-if="!districtExtraData.planningImageUrl">{{ districtDetail.projectName }}</span>
            <span class="map-subtitle" v-if="!districtExtraData.planningImageUrl">项目规划图</span>
          </div>
          <div v-if="authStore.isAdmin" style="margin-top: 10px;text-align:center;">
            <el-upload :show-file-list="false" :http-request="uploadDistrictImage" accept="image/*">
              <el-button size="small" type="primary" plain>上传项目规划图</el-button>
            </el-upload>
          </div>
        </div>
        <div class="district-info-section">
          <h4 class="section-title">项目简介</h4>
          <p class="district-desc" v-if="!authStore.isAdmin">{{ districtDetail.description }}</p>
          <el-input v-else type="textarea" v-model="districtDetail.description" :rows="3" />
        </div>
        <div class="district-info-section">
          <h4 class="section-title">项目详情</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">项目类型：</span>
              <span class="info-value" v-if="!authStore.isAdmin">{{ districtDetail.projectType || '片区规划' }}</span>
              <el-select v-else size="small" v-model="districtDetail.projectType" style="width: 100%;">
                <el-option v-for="c in categories" :key="c.itemCode" :label="c.itemName" :value="c.itemName" />
              </el-select>
            </div>
            <div class="info-item">
              <span class="info-label">开发面积：</span>
              <span class="info-value" v-if="!authStore.isAdmin">{{ districtExtraData.开发面积 || '待定' }}</span>
              <el-input v-else size="small" v-model="districtExtraData.开发面积" />
            </div>
            <div class="info-item">
              <span class="info-label">投资金额：</span>
              <span class="info-value" v-if="!authStore.isAdmin">{{ districtExtraData.投资金额 || '待定' }}</span>
              <el-input v-else size="small" v-model="districtExtraData.投资金额" />
            </div>
            <div class="info-item">
              <span class="info-label">片区类型：</span>
              <span class="info-value" v-if="!authStore.isAdmin">{{ districtExtraData.片区类型 || '综合开发' }}</span>
              <el-select v-else size="small" v-model="districtExtraData.片区类型" style="width: 100%;">
                <el-option v-for="s in subTypes" :key="s.itemCode" :label="s.itemName" :value="s.itemName" />
              </el-select>
            </div>
            <div class="info-item">
              <span class="info-label">规划周期：</span>
              <span class="info-value" v-if="!authStore.isAdmin">{{ districtDetail.startDate }} ~ {{ districtDetail.endDate }}</span>
              <div v-else style="display:flex;gap:5px;align-items:center;flex:1;">
                <el-date-picker v-model="districtDetail.startDate" type="date" size="small" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:110px"/>
                <span>~</span>
                <el-date-picker v-model="districtDetail.endDate" type="date" size="small" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:110px"/>
              </div>
            </div>
            <div class="info-item">
              <span class="info-label">当前状态：</span>
              <span class="info-value status-active" v-if="!authStore.isAdmin">{{ {PENDING:'待启动',IN_PROGRESS:'进行中',COMPLETED:'已完成',CANCELLED:'已取消'}[districtDetail.status] || '进行中' }}</span>
              <el-select v-else size="small" v-model="districtDetail.status" style="width: 100%;">
                <el-option label="待启动" value="PENDING" />
                <el-option label="进行中" value="IN_PROGRESS" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </div>
          </div>
        </div>
        
        <!-- 操作区 -->
        <div class="district-admin-actions" style="display:flex; gap:10px; justify-content:center; flex-wrap:wrap">
          <template v-if="authStore.isAdmin">
             <el-upload :show-file-list="false" :http-request="uploadDistrictFile" style="display:inline-block">
               <el-button type="primary" plain>详细规划(上传)</el-button>
             </el-upload>
             <el-button type="warning" plain @click="openDangerWorkForm">危险作业申报</el-button>
             <el-button type="success" @click="saveDistrictDetail">保存修改</el-button>
          </template>
          <template v-else>
             <el-button type="primary" plain @click="viewDistrictFiles">查看详细规划文件</el-button>
          </template>
        </div>
        <!-- 附件列表（图二效果） -->
        <div class="district-files-section" style="margin-top:20px; border-top: 1px solid rgba(255,255,255,0.1); padding-top:15px;">
          <h4 class="section-title">附件列表</h4>
          <div v-if="projectFiles.length === 0" class="no-files">暂无附件</div>
          <div v-for="f in projectFiles" :key="f.id" class="file-item-new">
            <div class="file-info-main">
              <el-icon class="file-type-icon"><component :is="getFileIcon(f.fileName)" /></el-icon>
              <div class="file-text">
                <span class="file-name-new" :title="f.fileName">{{ f.fileName }}</span>
                <span class="file-size-new">{{ ((f.fileSize || 0) / 1024).toFixed(1) }}KB</span>
              </div>
            </div>
            <div class="file-actions-new">
              <el-tooltip content="预览" placement="top">
                <el-button circle size="small" type="primary" :icon="View" @click="openFilePreview(f)" />
              </el-tooltip>
              <el-tooltip content="下载" placement="top">
                <a :href="getFileDownloadUrl(f.id!)" target="_blank" class="icon-link-btn">
                  <el-button circle size="small" type="success" :icon="Download" />
                </a>
              </el-tooltip>
              <el-tooltip v-if="authStore.isAdmin" content="删除" placement="top">
                <el-button circle size="small" type="danger" :icon="Delete" @click="deleteProjectFile(f.id!)" />
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 8. ECharts 数据面板 -->
    <!-- 土地储备 -->
    <div v-if="activeModule === 'land'" class="dashboard-overlay">
      <!-- 左面板: 土地储备总览 -->
      <div class="chart-panel left-panel" style="width: 420px; overflow-y: auto;">
        <div class="panel-title">土地储备总览</div>
        <div style="padding: 16px;">
          <!-- ① 总结语 -->
          <div class="land-summary-text">
            土地储备规模概览，共拿地 <b style="color:#00e5ff">{{ landProjects.length }}</b> 块，
            总面积 <b style="color:#00e5ff">{{ landTotalArea }}</b> 亩，
            总投资 <b style="color:#00e5ff">{{ (landTotalInvestment / 10000).toFixed(2) }}</b> 亿元，
            其中在建地块 <b style="color:#ffb800">{{ landInProgress }}</b> 个，
            已完成 <b style="color:#67c23a">{{ landCompleted }}</b> 个。
          </div>

          <!-- ② 拿地统计 -->
          <h4 class="land-section-label">◇◇ 拿地统计</h4>
          <div class="land-stat-cards">
            <div class="land-stat-card">
              <div class="land-stat-value">{{ landProjects.length }}</div>
              <div class="land-stat-label">拿地总块数 <span style="font-size:10px; color:#888">个</span></div>
            </div>
            <div class="land-stat-card">
              <div class="land-stat-value">{{ landTotalArea }}</div>
              <div class="land-stat-label">拿地总面积 <span style="font-size:10px; color:#888">亩</span></div>
            </div>
          </div>

          <!-- ③ 用地结构 饼图 -->
          <h4 class="land-section-label">◇◇ 用地结构</h4>
          <div ref="landPieChartRef" style="width: 100%; height: 220px;"></div>

          <!-- ④ 用地统计表格 -->
          <h4 class="land-section-label">◇◇ 用地统计</h4>
          <div class="land-table-wrapper">
            <table class="land-table">
              <thead>
                <tr>
                  <th>用地类型</th>
                  <th>地块数</th>
                  <th>面积(亩)</th>
                  <th>占比</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in landSubTypeStats" :key="item.subType">
                  <td>{{ landSubTypeNameMap[item.subType] || item.subType || '未分类' }}</td>
                  <td>{{ item.count }}</td>
                  <td>{{ (Number(item.totalArea || 0) / 666.67).toFixed(1) }}</td>
                  <td>{{ Number(landTotalArea) > 0 ? ((Number(item.totalArea || 0) / 666.67 / Number(landTotalArea)) * 100).toFixed(1) : 0 }}%</td>
                </tr>
                <tr class="land-table-total">
                  <td><b>合计</b></td>
                  <td><b>{{ landProjects.length }}</b></td>
                  <td><b>{{ landTotalArea }}</b></td>
                  <td><b>100%</b></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 右面板: 地块详情 (点击后显示) -->
      <div v-if="landDetailVisible" class="chart-panel right-panel" style="width: 400px; overflow-y: auto;">
        <div class="panel-title" style="display:flex; justify-content:space-between; align-items:center;">
          <span>地块详情</span>
          <el-icon class="close-btn" @click="landDetailVisible = false" style="cursor:pointer; font-size:18px;"><Close /></el-icon>
        </div>
        <div style="padding: 16px;">
          <div class="land-detail-row"><span class="land-detail-label">地块名称</span><span class="land-detail-val">{{ landDetail?.projectName }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">用地性质</span><span class="land-detail-val">{{ landSubTypeNameMap[landDetail?.subType || ''] || landDetail?.subType || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">面积(亩)</span><span class="land-detail-val">{{ landDetail?.area ? (Number(landDetail.area) / 666.67).toFixed(1) : '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">总投资(万)</span><span class="land-detail-val">{{ landDetail?.totalInvestment || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">所属区县</span><span class="land-detail-val">{{ landDetail?.district || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">项目状态</span>
            <span class="land-detail-val">
              <el-tag :type="landDetail?.status === 'COMPLETED' ? 'success' : landDetail?.status === 'IN_PROGRESS' ? 'warning' : 'info'" size="small">
                {{ {PENDING:'待启动', IN_PROGRESS:'进行中', COMPLETED:'已完成', CANCELLED:'已取消'}[landDetail?.status || ''] || '-' }}
              </el-tag>
            </span>
          </div>
          <div class="land-detail-row"><span class="land-detail-label">拿地时间</span><span class="land-detail-val">{{ landDetail?.startDate || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">地址</span><span class="land-detail-val">{{ landDetail?.address || '-' }}</span></div>
          <div class="land-detail-row" style="flex-direction:column; align-items:flex-start;">
            <span class="land-detail-label" style="margin-bottom:6px;">描述</span>
            <span class="land-detail-val" style="white-space:pre-wrap; line-height:1.6;">{{ landDetail?.description || '暂无描述' }}</span>
          </div>

          <el-divider style="margin: 15px 0" />
          <div class="detail-section-title" style="margin-bottom:10px;">
            <span style="font-weight: bold; color: #fff;">附件列表</span>
            <el-upload v-if="authStore.isAdmin" :show-file-list="false" :http-request="uploadLandFile" style="display:inline-block;margin-left:8px">
              <el-button size="small" type="primary">上传文件</el-button>
            </el-upload>
          </div>
          <div v-if="landFiles.length === 0" class="no-files">暂无附件</div>
          <div v-for="f in landFiles" :key="f.id" class="file-item-new">
            <div class="file-info-main">
              <el-icon class="file-type-icon"><component :is="getFileIcon(f.fileName)" /></el-icon>
              <div class="file-text">
                <span class="file-name-new" :title="f.fileName">{{ f.fileName }}</span>
                <span class="file-size-new">{{ ((f.fileSize || 0) / 1024).toFixed(1) }}KB</span>
              </div>
            </div>
            <div class="file-actions-new">
              <el-tooltip content="预览" placement="top">
                <el-button circle size="small" type="primary" :icon="View" @click="openFilePreview(f)" />
              </el-tooltip>
              <el-tooltip content="下载" placement="top">
                <a :href="getFileDownloadUrl(f.id!)" target="_blank" class="icon-link-btn">
                  <el-button circle size="small" type="success" :icon="Download" />
                </a>
              </el-tooltip>
              <el-tooltip v-if="authStore.isAdmin" content="删除" placement="top">
                <el-button circle size="small" type="danger" :icon="Delete" @click="deleteLandFile(f.id!)" />
              </el-tooltip>
            </div>
          </div>

          <div v-if="authStore.isAdmin" class="detail-actions" style="margin-top: 20px; text-align: center;">
            <el-button type="primary" style="width:100%" @click="editLandFromMap">编辑项目</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 项目建设 -->
    <div v-if="activeModule === 'project'" class="dashboard-overlay">
      <!-- 左面板: 项目建设总览 -->
      <div class="chart-panel left-panel" style="width: 420px; overflow-y: auto;">
        <div class="panel-title">项目建设总览</div>
        <div style="padding: 16px;">
          <!-- ① 总结语 -->
          <div class="land-summary-text">
            建设项目总投资 <b style="color:#00e5ff">{{ (engTotalInvestment / 10000).toFixed(2) }}</b> 亿元，
            共 <b style="color:#00e5ff">{{ engProjects.length }}</b> 个项目，
            其中市政类 <b style="color:#ffb800">{{ engSubCount('SHIZHENG') }}</b> 个，
            公建类 <b style="color:#67c23a">{{ engSubCount('GONGJIAN') }}</b> 个，
            住宅类 <b style="color:#f56c6c">{{ engSubCount('FANGDICHAN') }}</b> 个，
            代建类 <b style="color:#e6a23c">{{ engSubCount('DAIJIAN') }}</b> 个，
            在建项目 <b style="color:#ffb800">{{ engStatusCount('IN_PROGRESS') }}</b> 个，
            新开工项目 <b style="color:#67c23a">{{ engStatusCount('PENDING') }}</b> 个，
            已完成 <b style="color:#909399">{{ engStatusCount('COMPLETED') }}</b> 个。
          </div>

          <!-- ② 项目规模 -->
          <h4 class="land-section-label">◇◇ 项目规模</h4>
          <div class="land-stat-cards">
            <div class="land-stat-card">
              <div class="land-stat-value">{{ engProjects.length }}</div>
              <div class="land-stat-label">项目总数量 <span style="font-size:10px; color:#888">个</span></div>
            </div>
            <div class="land-stat-card">
              <div class="land-stat-value">{{ engTotalInvestment.toFixed(0) }}</div>
              <div class="land-stat-label">项目总投资 <span style="font-size:10px; color:#888">万元</span></div>
            </div>
          </div>

          <!-- ③ 项目状态 柱状图 -->
          <h4 class="land-section-label">◇◇ 项目状态</h4>
          <div ref="engBarChartRef" style="width: 100%; height: 220px;"></div>

          <!-- ④ 项目列表 (滚动) -->
          <h4 class="land-section-label">◇◇ 项目列表</h4>
          <div class="land-table-wrapper" style="max-height: 200px; overflow-y: auto;">
            <table class="land-table">
              <thead>
                <tr>
                  <th>项目名称</th>
                  <th>项目子类</th>
                  <th>项目状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in engProjects" :key="p.id" style="cursor:pointer" @click="onEngEntityClick(p)">
                  <td>{{ p.projectName }}</td>
                  <td>{{ engSubTypeNameMap[p.subType] || p.subType || '-' }}</td>
                  <td>
                    <el-tag :type="p.status === 'COMPLETED' ? 'success' : p.status === 'IN_PROGRESS' ? 'warning' : 'info'" size="small">
                      {{ {PENDING:'待启动', IN_PROGRESS:'进行中', COMPLETED:'已完成', CANCELLED:'已取消'}[p.status] || '-' }}
                    </el-tag>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 底部子类过滤按钮 -->
      <div class="eng-filter-bar">
        <div
          v-for="st in engSubTypeList"
          :key="st.itemCode"
          class="eng-filter-btn"
          :class="{ active: engActiveSubType === st.itemCode }"
          @click="toggleEngSubType(st.itemCode)"
        >{{ st.itemName }}</div>
      </div>

      <!-- 右面板: 项目详情 (点击后显示) -->
      <div v-if="engDetailVisible" class="chart-panel right-panel" style="width: 420px; overflow-y: auto;">
        <div class="panel-title" style="display:flex; justify-content:space-between; align-items:center;">
          <span>项目详情</span>
          <el-icon class="close-btn" @click="engDetailVisible = false" style="cursor:pointer; font-size:18px;"><Close /></el-icon>
        </div>
        <div style="padding: 16px;">
          <!-- 投资额柱状图 -->
          <div ref="engDetailChartRef" style="width: 100%; height: 180px; margin-bottom: 10px;"></div>

          <div class="land-detail-row"><span class="land-detail-label">项目名称</span><span class="land-detail-val">{{ engDetail?.projectName || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">所属二级集团</span><span class="land-detail-val">{{ engDetail?.secondaryGroup || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">施工单位</span><span class="land-detail-val">{{ engDetail?.constructionUnit || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">建设单位</span><span class="land-detail-val">{{ engDetail?.developmentUnit || '-' }}</span></div>
          <div class="land-detail-row"><span class="land-detail-label">资金来源</span><span class="land-detail-val">{{ engDetail?.fundSource || '-' }}</span></div>
          <div class="land-detail-row">
            <span class="land-detail-label">项目状态</span>
            <span class="land-detail-val">
              <el-tag :type="engDetail?.status === 'COMPLETED' ? 'success' : engDetail?.status === 'IN_PROGRESS' ? 'warning' : 'info'" size="small">
                {{ {PENDING:'待启动', IN_PROGRESS:'在建中', COMPLETED:'已完成', CANCELLED:'已取消'}[engDetail?.status || ''] || '-' }}
              </el-tag>
            </span>
          </div>
          <div class="land-detail-row" style="align-items: flex-start;">
            <span class="land-detail-label">手续办理情况</span>
            <span class="land-detail-val" style="word-break: break-all;">
              <template v-if="engDetail?.proceduresStatus">
                <div v-for="(url, idx) in engDetail.proceduresStatus.split(',').filter(Boolean)" :key="idx" style="margin-bottom: 4px;">
                  <a :href="url" target="_blank" style="color: #409EFF; text-decoration: none;">📄 附件{{ idx + 1 }}</a>
                </div>
              </template>
              <template v-else>-</template>
            </span>
          </div>
          <div class="land-detail-row" style="align-items: flex-start;">
            <span class="land-detail-label">竣工手续办理</span>
            <span class="land-detail-val" style="word-break: break-all;">
              <template v-if="engDetail?.completionProcedures">
                <div v-for="(url, idx) in engDetail.completionProcedures.split(',').filter(Boolean)" :key="idx" style="margin-bottom: 4px;">
                  <a :href="url" target="_blank" style="color: #409EFF; text-decoration: none;">📄 附件{{ idx + 1 }}</a>
                </div>
              </template>
              <template v-else>-</template>
            </span>
          </div>

          <el-divider style="margin: 10px 0; border-color: rgba(0,229,255,0.15);" />

          <div class="land-detail-row" style="flex-direction:column; align-items:flex-start;">
            <span class="land-detail-label" style="margin-bottom:6px;">工程进展</span>
            <span class="land-detail-val" style="white-space:pre-wrap; line-height:1.6;">{{ engDetail?.description || '暂无描述' }}</span>
          </div>
          <div class="land-detail-row" style="flex-direction:column; align-items:flex-start;">
            <span class="land-detail-label" style="margin-bottom:6px; color:#f56c6c;">存在问题</span>
            <span class="land-detail-val" style="white-space:pre-wrap; line-height:1.6; color: #f56c6c;">{{ engDetail?.existingProblems || '暂无' }}</span>
          </div>

          <el-divider style="margin: 10px 0; border-color: rgba(0,229,255,0.15);" />
          <div class="detail-section-title" style="margin-bottom:10px;">
            <span style="font-weight: bold; color: #fff;">附件列表</span>
            <el-upload v-if="authStore.isAdmin" :show-file-list="false" :http-request="uploadEngFile" style="display:inline-block;margin-left:8px">
              <el-button size="small" type="primary">上传文件</el-button>
            </el-upload>
          </div>
          <div v-if="engFiles.length === 0" class="no-files">暂无附件</div>
          <div v-for="f in engFiles" :key="f.id" class="file-item-new">
            <div class="file-info-main">
              <el-icon class="file-type-icon"><component :is="getFileIcon(f.fileName)" /></el-icon>
              <div class="file-text">
                <span class="file-name-new" :title="f.fileName">{{ f.fileName }}</span>
                <span class="file-size-new">{{ ((f.fileSize || 0) / 1024).toFixed(1) }}KB</span>
              </div>
            </div>
            <div class="file-actions-new">
              <el-tooltip content="预览" placement="top">
                <el-button circle size="small" type="primary" :icon="View" @click="openFilePreview(f)" />
              </el-tooltip>
              <el-tooltip content="下载" placement="top">
                <a :href="getFileDownloadUrl(f.id!)" target="_blank" class="icon-link-btn">
                  <el-button circle size="small" type="success" :icon="Download" />
                </a>
              </el-tooltip>
              <el-tooltip v-if="authStore.isAdmin" content="删除" placement="top">
                <el-button circle size="small" type="danger" :icon="Delete" @click="deleteEngFile(f.id!)" />
              </el-tooltip>
            </div>
          </div>

          <div v-if="authStore.isAdmin" class="detail-actions" style="margin-top: 20px; text-align: center;">
            <el-button type="primary" style="width:100%" @click="editEngFromMap">编辑项目</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 房产租售 -->
    <div v-show="activeModule === 'property'" class="dashboard-overlay">
      <div class="chart-panel left-panel">
        <div class="panel-title">房产分布</div>
        <div class="chart-container" ref="propertyChart1"></div>
      </div>
      <div class="chart-panel right-panel">
        <div class="panel-title">租售收益</div>
        <div class="chart-container" ref="propertyChart2"></div>
      </div>
    </div>
    
    <!-- 危险作业模块大屏 -->
    <div v-show="activeModule === 'danger'" class="dashboard-overlay">
      <!-- 左侧面板 -->
      <div class="chart-panel left-panel danger-left-panel" style="overflow-y:auto; padding: 16px;">
        <div class="panel-title">危险作业统计概览</div>
        <div class="danger-stat-list">
          <div class="danger-stat-item">
            <span class="danger-stat-label">危险作业总量</span>
            <span class="danger-stat-num danger-red">{{ dangerStats.total || 0 }}</span>
          </div>
          <div class="danger-stat-item">
            <span class="danger-stat-label">正在作业数量</span>
            <span class="danger-stat-num danger-yellow">{{ dangerStats.inProgress || 0 }}</span>
          </div>
          <div class="danger-stat-item">
            <span class="danger-stat-label">已完成数量</span>
            <span class="danger-stat-num danger-green">{{ dangerStats.completed || 0 }}</span>
          </div>
          <div class="danger-stat-item">
            <span class="danger-stat-label">已取消数量</span>
            <span class="danger-stat-num danger-blue">{{ dangerStats.cancelled || 0 }}</span>
          </div>
        </div>

        <div class="panel-title" style="margin-top:16px;">安全记录检查表</div>
        <div class="danger-record-list">
          <div v-for="rec in dangerRecentRecords" :key="rec.id" class="danger-record-item">
            <div class="record-time">{{ rec.createdAt?.slice(0,16).replace('T',' ') }}</div>
            <div class="record-name">{{ rec.workName }}</div>
            <el-tag
              :type="rec.status === 'COMPLETED' ? 'success' : rec.status === 'IN_PROGRESS' ? 'warning' : 'info'"
              size="small"
              effect="dark">
              {{ { IN_PROGRESS:'进行中', COMPLETED:'已完成', CANCELLED:'已取消' }[rec.status!] || '-' }}
            </el-tag>
          </div>
          <div v-if="dangerRecentRecords.length === 0" style="color:rgba(255,255,255,0.5);text-align:center;padding:20px;">暂无记录</div>
        </div>

        <!-- 管理员展开历史记录按钟 -->
        <div v-if="authStore.isAdmin" style="margin-top:16px;text-align:center;">
          <el-button
            type="warning"
            plain
            style="width:100%;font-size:13px;"
            @click="openDangerHistory">
            📜 危险作业历史记录（管理员）
          </el-button>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="chart-panel right-panel danger-right-panel" style="overflow-y:auto; padding: 16px;">
        <div class="panel-title">安全风险等级分布</div>
        <div ref="dangerRiskChart" style="width:100%;height:200px;"></div>

        <div class="panel-title" style="margin-top:8px;">作业类型统计分析</div>
        <div ref="dangerTypeChart" style="width:100%;height:220px;"></div>

        <div class="panel-title" style="margin-top:8px;">安全态势趋势图</div>
        <div ref="dangerTrendChart" style="width:100%;height:220px;"></div>
      </div>
    </div>

    <!-- ===== 危险作业申报弹窗 ===== -->
    <el-dialog
      v-model="dangerWorkFormVisible"
      title="危险作业申报"
      width="580px"
      class="dark-dialog"
      append-to-body
      destroy-on-close>
      <el-form :model="dangerWorkForm" label-width="100px" label-position="left">
        <el-form-item label="所属片区">
          <el-input :value="dangerWorkForm.districtName" disabled />
        </el-form-item>
        <el-form-item label="作业类型" required>
          <el-select v-model="dangerWorkForm.workType" placeholder="请选择作业类型" style="width:100%">
            <el-option label="高空作业" value="高空作业" />
            <el-option label="受限空间作业" value="受限空间作业" />
            <el-option label="危化品储存" value="危化品储存" />
            <el-option label="起重作业" value="起重作业" />
            <el-option label="明火作业" value="明火作业" />
          </el-select>
        </el-form-item>
        <el-form-item label="作业名称" required>
          <el-input v-model="dangerWorkForm.workName" placeholder="请输入作业名称" />
        </el-form-item>
        <el-form-item label="风险等级" required>
          <el-select v-model="dangerWorkForm.riskLevel" style="width:100%">
            <el-option label="重大风险" value="重大风险" />
            <el-option label="较大风险" value="较大风险" />
            <el-option label="一般风险" value="一般风险" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="dangerWorkForm.applicant" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="dangerWorkForm.responsiblePerson" />
        </el-form-item>
        <el-form-item label="实施主体">
          <el-input v-model="dangerWorkForm.implementor" />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input v-model="dangerWorkForm.locationDesc" />
        </el-form-item>
        <el-form-item label="作业坐标">
          <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
            <el-button
              v-if="!dangerPickingMode"
              type="primary" size="small" plain
              @click="startDangerPicking">
              📍 在地图上选点
            </el-button>
            <el-tag v-if="dangerPickingMode" type="warning">
              请在地图上单击选择坐标…
            </el-tag>
            <span v-if="dangerWorkForm.lng && dangerWorkForm.lat" style="color:#67c23a;font-size:12px;">
              {{ dangerWorkForm.lng?.toFixed(5) }}, {{ dangerWorkForm.lat?.toFixed(5) }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="计划时间">
          <div style="display:flex;gap:8px;align-items:center;">
            <el-date-picker v-model="dangerWorkForm.planStartTime" type="datetime" size="small" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:180px" />
            <span style="color:#aaa;">~</span>
            <el-date-picker v-model="dangerWorkForm.planEndTime" type="datetime" size="small" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:180px" />
          </div>
        </el-form-item>
        <el-form-item label="安全措施">
          <el-checkbox-group v-model="dangerWorkFormMeasures">
            <el-checkbox label="设备检查确认" />
            <el-checkbox label="警戒区域设置" />
            <el-checkbox label="指挥信号规范" />
            <el-checkbox label="吊具安全检查" />
            <el-checkbox label="作业人员培训" />
            <el-checkbox label="通风措施" />
            <el-checkbox label="灭火器配备" />
            <el-checkbox label="气体检测" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="作业描述">
          <el-input v-model="dangerWorkForm.workDescription" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dangerWorkFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="dangerWorkSubmitting" @click="submitDangerWork">提交申报</el-button>
      </template>
    </el-dialog>

    <!-- ===== 危险作业监控详情弹窗 ===== -->
    <el-dialog
      v-model="dangerMonitorVisible"
      :title="'监控详情 — ' + (dangerMonitorDetail?.monitorCode || '')"
      width="900px"
      class="dark-dialog monitor-dialog"
      append-to-body
      destroy-on-close
      @close="stopWebcam">
      <div v-if="dangerMonitorDetail" class="monitor-popup-body">
        <!-- 左侧摄像头 -->
        <div class="monitor-video-area">
          <video ref="webcamVideoRef" autoplay muted playsinline style="width:100%;height:100%;object-fit:cover;border-radius:8px;background:#000;"></video>
          <div class="webcam-controls">
            <el-button v-if="!isRecording" type="primary" size="small" @click="startRecording">⏺ 开始录制</el-button>
            <el-button v-else type="danger" size="small" @click="stopRecording">⏹ 停止并保存</el-button>
            <el-tag v-if="isRecording" type="danger" effect="dark" style="margin-left:8px;">录制中...</el-tag>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="monitor-info-area">
          <div class="monitor-info-card">
            <div class="monitor-info-title" style="color:#00d4ff;">监控点信息</div>
            <div class="monitor-info-row"><span>监控点编号：</span><strong>{{ dangerMonitorDetail.monitorCode }}</strong></div>
            <div class="monitor-info-row"><span>监控类型：</span><strong>{{ dangerMonitorDetail.workType }}</strong></div>
            <div class="monitor-info-row"><span>安装位置：</span><strong>{{ dangerMonitorDetail.locationDesc || '-' }}</strong></div>
            <div class="monitor-info-row">
              <span>监控状态：</span>
              <el-tag :type="dangerMonitorDetail.status === 'IN_PROGRESS' ? 'warning' : 'success'" size="small" effect="dark">
                {{ { IN_PROGRESS:'监控中', COMPLETED:'已完成', CANCELLED:'已取消' }[dangerMonitorDetail.status!] || '-' }}
              </el-tag>
            </div>
            <div class="monitor-info-row"><span>最后更新：</span><strong>{{ dangerMonitorDetail.updatedAt?.slice(0,19).replace('T',' ') }}</strong></div>
            <div class="monitor-info-row"><span>负责人：</span><strong>{{ dangerMonitorDetail.responsiblePerson || '-' }}</strong></div>
          </div>

          <div class="monitor-info-card" style="margin-top:12px;">
            <div class="monitor-info-title" style="color:#e6a23c;">危险作业详情</div>
            <div class="monitor-risk-alert" :class="dangerMonitorDetail.riskLevel === '重大风险' ? 'risk-critical' : dangerMonitorDetail.riskLevel === '较大风险' ? 'risk-major' : 'risk-normal'">
              ⚠ {{ dangerMonitorDetail.riskLevel }}
              <div style="font-size:12px;margin-top:6px;font-weight:normal;opacity:0.9;">{{ dangerMonitorDetail.workDescription }}</div>
            </div>

            <div class="monitor-info-title" style="color:#67c23a;margin-top:10px;">安全措施</div>
            <div v-for="m in (dangerMonitorDetail.safetyMeasures || '').split(',').filter(Boolean)" :key="m" class="safety-measure-item">
              ✅ {{ m }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- ===== 危险作业历史记录（管理员专属）===== -->
    <el-dialog
      v-model="dangerHistoryVisible"
      title="危险作业历史记录"
      width="960px"
      class="dark-dialog"
      append-to-body
      destroy-on-close>
      <el-table
        :data="dangerHistoryList"
        style="width:100%;"
        :header-cell-style="{background:'rgba(0,21,41,0.8)',color:'#00d4ff',borderColor:'rgba(0,212,255,0.2)'}"
        :cell-style="{background:'rgba(0,15,30,0.6)',color:'#e0e0e0',borderColor:'rgba(255,255,255,0.06)'}"
        v-loading="dangerHistoryLoading">
        <el-table-column prop="workCode" label="申报编号" width="155" />
        <el-table-column prop="districtName" label="所属片区" width="115" />
        <el-table-column prop="workType" label="作业类型" width="115" />
        <el-table-column prop="workName" label="作业名称" />
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{row}">
            <el-tag
              :type="row.riskLevel === '重大风险' ? 'danger' : row.riskLevel === '较大风险' ? 'warning' : 'success'"
              size="small">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 'IN_PROGRESS' ? 'warning' : row.status === 'COMPLETED' ? 'success' : 'info'" size="small">
              {{ { IN_PROGRESS:'进行中', COMPLETED:'已完成', CANCELLED:'已取消' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="90" />
        <el-table-column label="计划时间" width="200">
          <template #default="{row}">
            {{ row.planStartTime?.slice(0,10) }} ~ {{ row.planEndTime?.slice(0,10) || '未设置' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 资金信息 -->
    <div v-show="activeModule === 'fund'" class="dashboard-overlay">
      <div class="chart-panel left-panel">
        <div class="panel-title">资金流向</div>
        <div class="chart-container" ref="fundChart1"></div>
      </div>
      <div class="chart-panel right-panel">
        <div class="panel-title">收支统计</div>
        <div class="chart-container" ref="fundChart2"></div>
      </div>
    </div>
    <!-- 文件预览弹窗 (使用 vue-office) -->
    <el-dialog
      v-model="previewVisible"
      :title="previewTitle"
      width="80%"
      top="5vh"
      class="preview-dialog"
      append-to-body
      destroy-on-close>
      <div style="height: 80vh; overflow: auto; background-color: #f5f5f5;">
        <vue-office-docx v-if="previewType === 'docx'" :src="previewUrl" />
        <vue-office-excel v-if="previewType === 'excel'" :src="previewUrl" />
        <vue-office-pdf v-if="previewType === 'pdf'" :src="previewUrl" />
        <vue-office-pptx v-if="previewType === 'pptx'" :src="previewUrl" />
        <div v-if="previewType === 'image'" style="display:flex; justify-content:center; align-items:center; height:100%">
          <img :src="previewUrl" style="max-width:100%; max-height:100%; object-fit:contain" />
        </div>
        <div v-if="previewType === 'unsupported'" style="display:flex; justify-content:center; align-items:center; height:100%; color:#999">
          该文件类型暂不支持在线预览，请直接下载。
        </div>
      </div>
    </el-dialog>
    
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// Vue Office
import VueOfficeDocx from '@vue-office/docx'
import '@vue-office/docx/lib/index.css'
import VueOfficeExcel from '@vue-office/excel'
import '@vue-office/excel/lib/index.css'
import VueOfficePdf from '@vue-office/pdf'
import VueOfficePptx from '@vue-office/pptx'
import { 
  Promotion, HomeFilled, View, OfficeBuilding, TrendCharts, DataLine, 
  UserFilled, Search, Location, Clock, MapLocation, Files, Tools, Close,
  Refresh, Plus, Minus, FullScreen, Aim, Position, Collection, Flag,
  Share, Grid, Delete, DataAnalysis, CircleCheck, PieChart,
  Place, House, Warning, Money, Edit, Picture, Download, Reading, Document, Monitor
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { config } from '@/config'
import * as Cesium from 'cesium'
import * as echarts from 'echarts'

const router = useRouter()
const authStore = useAuthStore()

// State
const searchQuery = ref('')
const showResults = ref(false)
const activeModule = ref<'home' | 'land' | 'project' | 'property' | 'danger' | 'fund'>('home')
const activePanel = ref('layers')
const selectedLayerId = ref('')
const layerOpacity = ref(1.0)
const checkedLayers = ref(['shijiazhuang', 'landReserve', 'constructionProject'])
const popupPosition = reactive({ x: 0, y: 0 })

// 底图与3D配置
const currentBasemap = ref('arcgis_img')
const showTerrain = ref(true)
const showBuildings = ref(true)
let buildingsPrimitive: any = null

const basemaps = [
  { id: 'arcgis_img', name: 'ArcGIS影像', icon: 'linear-gradient(135deg, #1c3b57, #336699)', label: '🌍', type: 'arcgis' },
  { id: 'tianditu_img', name: '天地图影像', icon: 'linear-gradient(135deg, #2b4c3e, #4d8066)', label: '🛰️', type: 'tianditu_img' },
  { id: 'tianditu_vec', name: '天地图电子', icon: 'linear-gradient(135deg, #d3d9df, #e9ecef)', label: '🗺️', type: 'tianditu_vec' },
  { id: 'gaode_vec', name: '高德电子', icon: 'linear-gradient(135deg, #f0e6d2, #fff7e6)', label: '📍', type: 'gaode' },
  { id: 'dark', name: '黑色底图', icon: 'linear-gradient(135deg, #121212, #2c2c2c)', label: '🌙', type: 'dark' },
  { id: 'blue', name: '蓝色底图', icon: 'linear-gradient(135deg, #0f2027, #203a43)', label: '🌌', type: 'blue' },
]

const cameraInfo = reactive({
  height: '0',
  longitude: '0.000000',
  latitude: '0.000000',
  pitch: '0'
})

// Tree Data
const layerTreeData = computed(() => [
  {
    id: 'root',
    label: '基础数据',
    children: [
      { id: 'shijiazhuang', label: '行政区划', icon: 'Location' }
    ]
  },
  {
    id: 'land',
    label: '土地储备',
    children: []
  },
  {
    id: 'projects',
    label: '工程项目',
    children: []
  },
  {
    id: 'property',
    label: '资产经营',
    children: []
  }
])

// Cesium
let viewer: Cesium.Viewer | null = null
const selectedEntity = ref<any>(null)
const SHIJIAZHUANG = { longitude: 114.48, latitude: 38.03, height: 50000 }

// ========== 工具箱状态 ==========
const activeTool = ref<string>('')

// ========== 土地储备模块 ==========
const landProjects = ref<any[]>([])
const landSubTypeStats = ref<any[]>([])
const landDetailVisible = ref(false)
const landDetail = ref<any>(null)
const landFiles = ref<any[]>([])
const landPieChartRef = ref<HTMLElement | null>(null)
let landPieChart: echarts.ECharts | null = null
const landSubTypeNameMap = ref<Record<string, string>>({})
const landSubTypes = ref<any[]>([])

const landTotalArea = computed(() => {
  const totalSqm = landProjects.value.reduce((sum: number, p: any) => sum + Number(p.area || 0), 0)
  return (totalSqm / 666.67).toFixed(1)
})
const landTotalInvestment = computed(() => {
  return landProjects.value.reduce((sum: number, p: any) => sum + Number(p.totalInvestment || 0), 0)
})
const landInProgress = computed(() => landProjects.value.filter((p: any) => p.status === 'IN_PROGRESS').length)
const landCompleted = computed(() => landProjects.value.filter((p: any) => p.status === 'COMPLETED').length)

// 量算工具
const measureMode = ref<'distance' | 'area' | null>(null)
const measureResult = ref('')
let measureHandler: Cesium.ScreenSpaceEventHandler | null = null
let measureEntities: Cesium.Entity[] = []
let measurePositions: Cesium.Cartesian3[] = []

// 坐标定位
const locateForm = reactive({
  longitude: 114.48,
  latitude: 38.03,
  height: 5000
})

// 视角书签
interface Bookmark {
  name: string
  longitude: number
  latitude: number
  height: number
  heading: number
  pitch: number
  roll: number
}
const bookmarks = ref<Bookmark[]>([])
const bookmarkDialogVisible = ref(false)
const newBookmark = reactive({ name: '' })

// 我的标记
interface MapMarker {
  name: string
  description: string
  longitude: number
  latitude: number
  entityId?: string
}
const markers = ref<MapMarker[]>([])
const markerDialogVisible = ref(false)
const isAddingMarker = ref(false)
const newMarker = reactive({ name: '', description: '', longitude: 0, latitude: 0 })
let markerHandler: Cesium.ScreenSpaceEventHandler | null = null

// 空间分析
const spatialMode = ref<'buffer' | 'query' | 'statistics' | null>(null)
const spatialResult = ref<any>(null)
const bufferRadius = ref(1000)
let spatialHandler: Cesium.ScreenSpaceEventHandler | null = null
let spatialEntities: Cesium.Entity[] = []
let statisticsPositions: Cesium.Cartesian3[] = []

// 图上标绘
interface DrawingData {
  id?: number
  name: string
  drawType: string
  category: string
  businessId?: number
  geometry: string
  area?: string
  length?: string
  description?: string
}
const drawMode = ref<'point' | 'polyline' | 'polygon' | null>(null)
const savedDrawings = ref<DrawingData[]>([])
const drawingDialogVisible = ref(false)
const drawingSaving = ref(false)
const newDrawing = reactive<DrawingData>({
  name: '',
  drawType: '',
  category: 'OTHER',
  geometry: '',
  description: ''
})
let drawHandler: Cesium.ScreenSpaceEventHandler | null = null
let drawPositions: Cesium.Cartesian3[] = []
let drawEntities: Cesium.Entity[] = []
let tempDrawingGeometry: any = null


// Methods
const handleCommand = (cmd: string) => {
  if (cmd === 'logout') {
    authStore.logout().then(() => router.push('/login'))
  }
}

const handleSearch = () => {
  if (!searchQuery.value) return
  showResults.value = true
  setTimeout(() => {
    ElMessage.success(`搜索: ${searchQuery.value}`)
  }, 500)
}

const togglePanel = (panel: string) => {
  activePanel.value = activePanel.value === panel ? '' : panel
}

const handleLayerCheck = async (data: any, status: any) => {
  if (!viewer || data.children) return 
  
  const layerId = data.id
  const isChecked = status.checkedKeys.includes(layerId)
  
  const layerConfig = config.layers.find(l => l.id === layerId)
  if (!layerConfig) return

  const ds = viewer.dataSources.getByName(layerId)[0]
  if (ds) {
    ds.show = isChecked
  } else if (isChecked) {
    try {
      const response = await fetch(`/${layerConfig.file}`)
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
      const geoJson = await response.json()
      
      if (geoJson.crs) delete geoJson.crs // Fix EPSG:4490 error
      
      const dataSource = await Cesium.GeoJsonDataSource.load(geoJson, {
        stroke: Cesium.Color.fromCssColorString('#409eff'),
        fill: Cesium.Color.fromCssColorString('rgba(64, 158, 255, 0.3)'),
        strokeWidth: 2
      })
      dataSource.name = layerId
      viewer.dataSources.add(dataSource)
    } catch (e) {
      console.error(`[图层] 加载失败: ${layerId}`, e)
    }
  }
}

const changeBasemap = async (map: any) => {
  if (!viewer) return
  currentBasemap.value = map.id
  
  viewer.imageryLayers.removeAll()
  
  try {
    switch (map.type) {
      case 'tianditu_img':
        addTiandituLayer('img')
        addTiandituLayer('cia')
        break
      case 'tianditu_vec':
        addTiandituLayer('vec')
        addTiandituLayer('cva')
        break
      case 'arcgis':
        const provider = await Cesium.ArcGisMapServerImageryProvider.fromUrl(
          'https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer'
        )
        viewer.imageryLayers.addImageryProvider(provider)
        break
      case 'gaode':
        viewer.imageryLayers.addImageryProvider(
          new Cesium.UrlTemplateImageryProvider({
            url: 'https://webrd02.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}',
            minimumLevel: 3,
            maximumLevel: 18
          })
        )
        break
      case 'dark':
        viewer.imageryLayers.addImageryProvider(
          new Cesium.UrlTemplateImageryProvider({
            url: 'https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png',
            credit: 'CartoDB'
          })
        )
        break
      case 'blue':
        viewer.imageryLayers.addImageryProvider(
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

const addTiandituLayer = (layer: string) => {
  const token = config.tiandituToken
  const url = `/tianditu/${layer}_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=${layer}&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${token}`
  
  viewer?.imageryLayers.addImageryProvider(
    new Cesium.UrlTemplateImageryProvider({
      url: url,
      maximumLevel: 18
    })
  )
}

const toggleTerrain = async (val: boolean) => {
  if (!viewer) return
  if (val) {
    try {
      viewer.terrainProvider = await Cesium.createWorldTerrainAsync({
        requestWaterMask: true,
        requestVertexNormals: true
      })
    } catch (e) {
      console.error('[地形] 加载失败', e)
      ElMessage.warning('无法加载 3D 地形，已切换为平面地图')
      viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider()
      showTerrain.value = false
    }
  } else {
    viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider()
  }
}

const toggleBuildings = async (val: boolean) => {
  if (!viewer) return
  
  if (val) {
    if (!buildingsPrimitive) {
      try {
        console.log('[地图] 加载 OSM 3D 建筑...')
        buildingsPrimitive = viewer.scene.primitives.add(await Cesium.createOsmBuildingsAsync({
             style: new Cesium.Cesium3DTileStyle({
                  color: {
                    conditions: [
                      ["${feature['building']} === 'apartments'", "color('#00ffff', 0.8)"],
                      ["${feature['building']} === 'commercial'", "color('#0096ff', 0.8)"],
                      ["true", "color('#ffffff', 0.7)"] 
                    ]
                  }
             })
        }))
        console.log('[地图] 3D 建筑加载完成')
      } catch (e) {
        console.error('[地图] 3D 建筑加载失败', e)
        ElMessage.error('无法加载 3D 建筑')
      }
    } else {
      buildingsPrimitive.show = true
    }
  } else {
    if (buildingsPrimitive) buildingsPrimitive.show = false
  }
}

const flyToShijiazhuang = () => {
  viewer?.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(SHIJIAZHUANG.longitude, SHIJIAZHUANG.latitude - 0.05, 5000),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-25),
      roll: 0
    },
    duration: 1.5
  })
  showResults.value = false
}

const resetCamera = () => {
  viewer?.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(SHIJIAZHUANG.longitude, SHIJIAZHUANG.latitude - 0.05, 5000),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-25),
      roll: 0
    }
  })
}

const zoomIn = () => viewer?.camera.zoomIn(viewer.camera.positionCartographic.height * 0.3)
const zoomOut = () => viewer?.camera.zoomOut(viewer.camera.positionCartographic.height * 0.3)

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const updateCameraInfo = () => {
  if (!viewer) return
  const c = viewer.camera
  const cartographic = c.positionCartographic
  cameraInfo.height = cartographic.height.toFixed(1)
  cameraInfo.longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(6)
  cameraInfo.latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(6)
  cameraInfo.pitch = Cesium.Math.toDegrees(c.pitch).toFixed(1)
}

// ========== 工具箱方法 ==========
const setActiveTool = (tool: string) => {
  // 切换工具时清除之前的状态
  if (activeTool.value !== tool) {
    clearMeasure()
    if (isAddingMarker.value) toggleMarkerMode()
  }
  activeTool.value = activeTool.value === tool ? '' : tool
}

// 量算工具
const startMeasure = (mode: 'distance' | 'area') => {
  if (!viewer) return
  clearMeasure()
  measureMode.value = mode
  measurePositions = []
  
  measureHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  
  // 左键点击添加点
  measureHandler.setInputAction((click: any) => {
    const ray = viewer!.camera.getPickRay(click.position)
    if (!ray) return
    const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
    if (!cartesian) return
    
    measurePositions.push(cartesian)
    
    // 添加点标记
    const entity = viewer!.entities.add({
      position: cartesian,
      point: {
        pixelSize: 8,
        color: Cesium.Color.YELLOW,
        outlineColor: Cesium.Color.BLACK,
        outlineWidth: 1
      }
    })
    measureEntities.push(entity)
    
    // 如果有2个以上点，绘制线段
    if (measurePositions.length >= 2) {
      const lineEntity = viewer!.entities.add({
        polyline: {
          positions: measurePositions.slice(-2),
          width: 3,
          material: Cesium.Color.YELLOW
        }
      })
      measureEntities.push(lineEntity)
      
      // 更新距离
      if (mode === 'distance') {
        measureResult.value = calculateDistance(measurePositions)
      }
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
  
  // 右键结束
  measureHandler.setInputAction(() => {
    if (measurePositions.length < 2) {
      ElMessage.warning('请至少添加2个点')
      return
    }
    
    if (mode === 'area' && measurePositions.length >= 3) {
      // 闭合多边形
      const polygonEntity = viewer!.entities.add({
        polygon: {
          hierarchy: new Cesium.PolygonHierarchy(measurePositions),
          material: Cesium.Color.YELLOW.withAlpha(0.3),
          outline: true,
          outlineColor: Cesium.Color.YELLOW
        }
      })
      measureEntities.push(polygonEntity)
      measureResult.value = calculateArea(measurePositions)
    }
    
    measureHandler?.destroy()
    measureHandler = null
    measureMode.value = null
  }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
}

const clearMeasure = () => {
  measureHandler?.destroy()
  measureHandler = null
  measureMode.value = null
  measureResult.value = ''
  measurePositions = []
  measureEntities.forEach(e => viewer?.entities.remove(e))
  measureEntities = []
}

const calculateDistance = (positions: Cesium.Cartesian3[]): string => {
  let total = 0
  for (let i = 1; i < positions.length; i++) {
    const geodesic = new Cesium.EllipsoidGeodesic(
      Cesium.Ellipsoid.WGS84.cartesianToCartographic(positions[i - 1]),
      Cesium.Ellipsoid.WGS84.cartesianToCartographic(positions[i])
    )
    total += geodesic.surfaceDistance
  }
  return total > 1000 ? `${(total / 1000).toFixed(2)} 公里` : `${total.toFixed(2)} 米`
}

const calculateArea = (positions: Cesium.Cartesian3[]): string => {
  const coords = positions.map(p => Cesium.Ellipsoid.WGS84.cartesianToCartographic(p))
  // 使用球面多边形面积公式（简化版）
  let area = 0
  const n = coords.length
  for (let i = 0; i < n; i++) {
    const j = (i + 1) % n
    area += coords[i].longitude * coords[j].latitude
    area -= coords[j].longitude * coords[i].latitude
  }
  area = Math.abs(area / 2) * Math.pow(6378137, 2) // 地球半径
  return area > 1000000 ? `${(area / 1000000).toFixed(4)} 平方公里` : `${area.toFixed(2)} 平方米`
}

// 坐标定位
const flyToCoordinate = () => {
  if (!viewer) return
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(locateForm.longitude, locateForm.latitude, locateForm.height),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-45),
      roll: 0
    },
    duration: 2
  })
  ElMessage.success(`已飞行至 ${locateForm.longitude.toFixed(4)}, ${locateForm.latitude.toFixed(4)}`)
}

// 视角书签
const saveBookmark = () => {
  newBookmark.name = `书签 ${bookmarks.value.length + 1}`
  bookmarkDialogVisible.value = true
}

const confirmSaveBookmark = () => {
  if (!viewer || !newBookmark.name.trim()) {
    ElMessage.warning('请输入书签名称')
    return
  }
  const camera = viewer.camera
  const cartographic = camera.positionCartographic
  
  bookmarks.value.push({
    name: newBookmark.name,
    longitude: Cesium.Math.toDegrees(cartographic.longitude),
    latitude: Cesium.Math.toDegrees(cartographic.latitude),
    height: cartographic.height,
    heading: camera.heading,
    pitch: camera.pitch,
    roll: camera.roll
  })
  
  localStorage.setItem('map_bookmarks', JSON.stringify(bookmarks.value))
  bookmarkDialogVisible.value = false
  newBookmark.name = ''
  ElMessage.success('书签已保存')
}

const flyToBookmark = (bm: Bookmark) => {
  viewer?.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(bm.longitude, bm.latitude, bm.height),
    orientation: {
      heading: bm.heading,
      pitch: bm.pitch,
      roll: bm.roll
    },
    duration: 1.5
  })
}

const deleteBookmark = (idx: number) => {
  bookmarks.value.splice(idx, 1)
  localStorage.setItem('map_bookmarks', JSON.stringify(bookmarks.value))
  ElMessage.success('书签已删除')
}

// 我的标记
const toggleMarkerMode = () => {
  if (!viewer) return
  isAddingMarker.value = !isAddingMarker.value
  
  if (isAddingMarker.value) {
    markerHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
    markerHandler.setInputAction((click: any) => {
      const ray = viewer!.camera.getPickRay(click.position)
      if (!ray) return
      const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
      if (!cartesian) return
      
      const cartographic = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesian)
      newMarker.longitude = Cesium.Math.toDegrees(cartographic.longitude)
      newMarker.latitude = Cesium.Math.toDegrees(cartographic.latitude)
      newMarker.name = `标记 ${markers.value.length + 1}`
      newMarker.description = ''
      markerDialogVisible.value = true
      
      // 临时禁用添加模式
      markerHandler?.destroy()
      markerHandler = null
      isAddingMarker.value = false
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
  } else {
    markerHandler?.destroy()
    markerHandler = null
  }
}

const confirmAddMarker = () => {
  if (!viewer || !newMarker.name.trim()) {
    ElMessage.warning('请输入标记名称')
    return
  }
  
  const marker: MapMarker = {
    name: newMarker.name,
    description: newMarker.description,
    longitude: newMarker.longitude,
    latitude: newMarker.latitude
  }
  
  // 添加到地图
  const entity = viewer.entities.add({
    position: Cesium.Cartesian3.fromDegrees(marker.longitude, marker.latitude),
    billboard: {
      image: '/img/marker.png',
      width: 32,
      height: 32,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM
    },
    label: {
      text: marker.name,
      font: '14px sans-serif',
      fillColor: Cesium.Color.WHITE,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      outlineWidth: 2,
      outlineColor: Cesium.Color.BLACK,
      verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      pixelOffset: new Cesium.Cartesian2(0, -36)
    }
  })
  
  marker.entityId = entity.id
  markers.value.push(marker)
  localStorage.setItem('map_markers', JSON.stringify(markers.value))
  
  markerDialogVisible.value = false
  ElMessage.success('标记已添加')
}

const flyToMarker = (mk: MapMarker) => {
  viewer?.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(mk.longitude, mk.latitude, 1000),
    duration: 1
  })
}

const deleteMarker = (idx: number) => {
  const mk = markers.value[idx]
  if (mk.entityId && viewer) {
    const entity = viewer.entities.getById(mk.entityId)
    if (entity) viewer.entities.remove(entity)
  }
  markers.value.splice(idx, 1)
  localStorage.setItem('map_markers', JSON.stringify(markers.value))
  ElMessage.success('标记已删除')
}

const loadToolsData = () => {
  // 从 localStorage 加载书签和标记
  const savedBookmarks = localStorage.getItem('map_bookmarks')
  if (savedBookmarks) {
    try {
      bookmarks.value = JSON.parse(savedBookmarks)
    } catch (e) {
      console.warn('[工具] 书签数据加载失败')
    }
  }
  
  const savedMarkers = localStorage.getItem('map_markers')
  if (savedMarkers) {
    try {
      markers.value = JSON.parse(savedMarkers)
      // 渲染已保存的标记到地图
      markers.value.forEach(mk => {
        if (viewer) {
          const entity = viewer.entities.add({
            position: Cesium.Cartesian3.fromDegrees(mk.longitude, mk.latitude),
            billboard: {
              image: '/img/marker.png',
              width: 32,
              height: 32,
              verticalOrigin: Cesium.VerticalOrigin.BOTTOM
            },
            label: {
              text: mk.name,
              font: '14px sans-serif',
              fillColor: Cesium.Color.WHITE,
              style: Cesium.LabelStyle.FILL_AND_OUTLINE,
              outlineWidth: 2,
              outlineColor: Cesium.Color.BLACK,
              verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
              pixelOffset: new Cesium.Cartesian2(0, -36)
            }
          })
          mk.entityId = entity.id
        }
      })
    } catch (e) {
      console.warn('[工具] 标记数据加载失败')
    }
  }
}

// ========== 空间分析方法 ==========
const startSpatialAnalysis = (mode: 'buffer' | 'query' | 'statistics') => {
  if (!viewer) return
  clearSpatialAnalysis()
  spatialMode.value = mode
  
  spatialHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  
  if (mode === 'buffer') {
    // 缓冲区分析：点击创建缓冲区
    spatialHandler.setInputAction((click: any) => {
      const ray = viewer!.camera.getPickRay(click.position)
      if (!ray) return
      const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
      if (!cartesian) return
      
      const cartographic = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesian)
      const lon = Cesium.Math.toDegrees(cartographic.longitude)
      const lat = Cesium.Math.toDegrees(cartographic.latitude)
      
      // 绘制缓冲区圆形
      const circleEntity = viewer!.entities.add({
        position: cartesian,
        ellipse: {
          semiMajorAxis: bufferRadius.value,
          semiMinorAxis: bufferRadius.value,
          material: Cesium.Color.CYAN.withAlpha(0.3),
          outline: true,
          outlineColor: Cesium.Color.CYAN,
          outlineWidth: 2
        }
      })
      spatialEntities.push(circleEntity)
      
      // 中心点标记
      const centerEntity = viewer!.entities.add({
        position: cartesian,
        point: {
          pixelSize: 10,
          color: Cesium.Color.RED,
          outlineColor: Cesium.Color.WHITE,
          outlineWidth: 2
        }
      })
      spatialEntities.push(centerEntity)
      
      // 统计缓冲区内的要素
      const featuresInBuffer = countFeaturesInRadius(lon, lat, bufferRadius.value)
      
      spatialResult.value = {
        type: 'buffer',
        center: `${lon.toFixed(6)}, ${lat.toFixed(6)}`,
        radius: bufferRadius.value,
        count: featuresInBuffer.count,
        features: featuresInBuffer.features
      }
      
      ElMessage.success(`缓冲区分析完成，找到 ${featuresInBuffer.count} 个要素`)
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
  } else if (mode === 'query') {
    // 点击查询：点击要素查看属性
    spatialHandler.setInputAction((click: any) => {
      const picked = viewer!.scene.pick(click.position)
      if (Cesium.defined(picked) && picked.id?.properties) {
        const props: Record<string, any> = {}
        picked.id.properties.propertyNames.forEach((n: string) => {
          props[n] = picked.id.properties[n]?.getValue()
        })
        spatialResult.value = {
          type: 'query',
          name: picked.id.name || '未命名要素',
          properties: props
        }
      } else {
        ElMessage.info('未点击到任何要素')
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
  } else if (mode === 'statistics') {
    // 区域统计：绘制多边形统计
    statisticsPositions = []
    
    spatialHandler.setInputAction((click: any) => {
      const ray = viewer!.camera.getPickRay(click.position)
      if (!ray) return
      const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
      if (!cartesian) return
      
      statisticsPositions.push(cartesian)
      
      // 添加顶点标记
      const pointEntity = viewer!.entities.add({
        position: cartesian,
        point: {
          pixelSize: 8,
          color: Cesium.Color.ORANGE,
          outlineColor: Cesium.Color.BLACK,
          outlineWidth: 1
        }
      })
      spatialEntities.push(pointEntity)
      
      // 绘制边线
      if (statisticsPositions.length >= 2) {
        const lineEntity = viewer!.entities.add({
          polyline: {
            positions: statisticsPositions.slice(-2),
            width: 2,
            material: Cesium.Color.ORANGE
          }
        })
        spatialEntities.push(lineEntity)
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
    // 右键结束绘制
    spatialHandler.setInputAction(() => {
      if (statisticsPositions.length < 3) {
        ElMessage.warning('请至少绘制3个顶点')
        return
      }
      
      // 绘制多边形
      const polygonEntity = viewer!.entities.add({
        polygon: {
          hierarchy: new Cesium.PolygonHierarchy(statisticsPositions),
          material: Cesium.Color.ORANGE.withAlpha(0.3),
          outline: true,
          outlineColor: Cesium.Color.ORANGE
        }
      })
      spatialEntities.push(polygonEntity)
      
      // 统计多边形内的要素
      const stats = countFeaturesInPolygon(statisticsPositions)
      const area = calculateArea(statisticsPositions)
      
      spatialResult.value = {
        type: 'statistics',
        area: area,
        count: stats.total,
        breakdown: stats.breakdown
      }
      
      ElMessage.success(`区域统计完成，共 ${stats.total} 个要素`)
      
      spatialHandler?.destroy()
      spatialHandler = null
      spatialMode.value = null
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
  }
}

const clearSpatialAnalysis = () => {
  spatialHandler?.destroy()
  spatialHandler = null
  spatialMode.value = null
  spatialResult.value = null
  statisticsPositions = []
  spatialEntities.forEach(e => viewer?.entities.remove(e))
  spatialEntities = []
}

// 统计指定半径内的要素
const countFeaturesInRadius = (centerLon: number, centerLat: number, radius: number) => {
  const features: any[] = []
  
  viewer?.dataSources.getByName('shijiazhuang').forEach(ds => {
    ds.entities.values.forEach(entity => {
      if (entity.position) {
        const pos = entity.position.getValue(Cesium.JulianDate.now())
        if (pos) {
          const cart = Cesium.Ellipsoid.WGS84.cartesianToCartographic(pos)
          const lon = Cesium.Math.toDegrees(cart.longitude)
          const lat = Cesium.Math.toDegrees(cart.latitude)
          const dist = haversineDistance(centerLat, centerLon, lat, lon)
          if (dist <= radius) {
            features.push({ name: entity.name, id: entity.id })
          }
        }
      }
    })
  })
  
  // 遍历所有数据源
  for (let i = 0; i < viewer!.dataSources.length; i++) {
    const ds = viewer!.dataSources.get(i)
    ds.entities.values.forEach(entity => {
      if (entity.polygon?.hierarchy) {
        // 对于多边形，检查质心
        const hierarchy = entity.polygon.hierarchy.getValue(Cesium.JulianDate.now())
        if (hierarchy && hierarchy.positions.length > 0) {
          const centroid = Cesium.BoundingSphere.fromPoints(hierarchy.positions).center
          const cart = Cesium.Ellipsoid.WGS84.cartesianToCartographic(centroid)
          const lon = Cesium.Math.toDegrees(cart.longitude)
          const lat = Cesium.Math.toDegrees(cart.latitude)
          const dist = haversineDistance(centerLat, centerLon, lat, lon)
          if (dist <= radius) {
            features.push({ name: entity.name || ds.name, id: entity.id })
          }
        }
      }
    })
  }
  
  return { count: features.length, features }
}

// 统计多边形内的要素
const countFeaturesInPolygon = (polygonPositions: Cesium.Cartesian3[]) => {
  const breakdown: Record<string, number> = {}
  let total = 0
  
  const polygonCoords = polygonPositions.map(p => {
    const cart = Cesium.Ellipsoid.WGS84.cartesianToCartographic(p)
    return { lon: Cesium.Math.toDegrees(cart.longitude), lat: Cesium.Math.toDegrees(cart.latitude) }
  })
  
  for (let i = 0; i < viewer!.dataSources.length; i++) {
    const ds = viewer!.dataSources.get(i)
    let count = 0
    
    ds.entities.values.forEach(entity => {
      let point: { lon: number, lat: number } | null = null
      
      if (entity.position) {
        const pos = entity.position.getValue(Cesium.JulianDate.now())
        if (pos) {
          const cart = Cesium.Ellipsoid.WGS84.cartesianToCartographic(pos)
          point = { lon: Cesium.Math.toDegrees(cart.longitude), lat: Cesium.Math.toDegrees(cart.latitude) }
        }
      } else if (entity.polygon?.hierarchy) {
        const hierarchy = entity.polygon.hierarchy.getValue(Cesium.JulianDate.now())
        if (hierarchy && hierarchy.positions.length > 0) {
          const centroid = Cesium.BoundingSphere.fromPoints(hierarchy.positions).center
          const cart = Cesium.Ellipsoid.WGS84.cartesianToCartographic(centroid)
          point = { lon: Cesium.Math.toDegrees(cart.longitude), lat: Cesium.Math.toDegrees(cart.latitude) }
        }
      }
      
      if (point && isPointInPolygon(point, polygonCoords)) {
        count++
        total++
      }
    })
    
    if (count > 0 && ds.name) {
      breakdown[ds.name] = count
    }
  }
  
  return { total, breakdown }
}

// Haversine 距离公式
const haversineDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
  const R = 6371000 // 地球半径（米）
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}

// 点是否在多边形内（射线法）
const isPointInPolygon = (point: { lon: number, lat: number }, polygon: { lon: number, lat: number }[]): boolean => {
  let inside = false
  const n = polygon.length
  for (let i = 0, j = n - 1; i < n; j = i++) {
    const xi = polygon[i].lon, yi = polygon[i].lat
    const xj = polygon[j].lon, yj = polygon[j].lat
    if (((yi > point.lat) !== (yj > point.lat)) && (point.lon < (xj - xi) * (point.lat - yi) / (yj - yi) + xi)) {
      inside = !inside
    }
  }
  return inside
}

// ========== 图上标绘方法 ==========
const startDrawing = (mode: 'point' | 'polyline' | 'polygon') => {
  if (!viewer) return
  clearDrawing()
  drawMode.value = mode
  drawPositions = []
  
  drawHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  
  if (mode === 'point') {
    drawHandler.setInputAction((click: any) => {
      const ray = viewer!.camera.getPickRay(click.position)
      if (!ray) return
      const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
      if (!cartesian) return
      
      drawPositions = [cartesian]
      
      const entity = viewer!.entities.add({
        position: cartesian,
        point: { pixelSize: 12, color: Cesium.Color.LIME, outlineColor: Cesium.Color.BLACK, outlineWidth: 2 }
      })
      drawEntities.push(entity)
      
      tempDrawingGeometry = toGeoJSONPoint(cartesian)
      newDrawing.drawType = 'POINT'
      newDrawing.name = `点标绘-${Date.now()}`
      drawingDialogVisible.value = true
      
      drawHandler?.destroy()
      drawHandler = null
      drawMode.value = null
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
  } else if (mode === 'polyline') {
    drawHandler.setInputAction((click: any) => {
      const ray = viewer!.camera.getPickRay(click.position)
      if (!ray) return
      const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
      if (!cartesian) return
      
      drawPositions.push(cartesian)
      
      const pointEntity = viewer!.entities.add({
        position: cartesian,
        point: { pixelSize: 8, color: Cesium.Color.LIME }
      })
      drawEntities.push(pointEntity)
      
      if (drawPositions.length >= 2) {
        const lineEntity = viewer!.entities.add({
          polyline: { positions: drawPositions.slice(-2), width: 3, material: Cesium.Color.LIME }
        })
        drawEntities.push(lineEntity)
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
    drawHandler.setInputAction(() => {
      if (drawPositions.length < 2) { ElMessage.warning('请至少绘制2个点'); return }
      
      tempDrawingGeometry = toGeoJSONLineString(drawPositions)
      newDrawing.drawType = 'POLYLINE'
      newDrawing.name = `线标绘-${Date.now()}`
      newDrawing.length = calculateDistance(drawPositions)
      drawingDialogVisible.value = true
      
      drawHandler?.destroy()
      drawHandler = null
      drawMode.value = null
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
    
  } else if (mode === 'polygon') {
    drawHandler.setInputAction((click: any) => {
      const ray = viewer!.camera.getPickRay(click.position)
      if (!ray) return
      const cartesian = viewer!.scene.globe.pick(ray, viewer!.scene)
      if (!cartesian) return
      
      drawPositions.push(cartesian)
      
      const pointEntity = viewer!.entities.add({
        position: cartesian,
        point: { pixelSize: 8, color: Cesium.Color.ORANGE }
      })
      drawEntities.push(pointEntity)
      
      if (drawPositions.length >= 2) {
        const lineEntity = viewer!.entities.add({
          polyline: { positions: drawPositions.slice(-2), width: 2, material: Cesium.Color.ORANGE }
        })
        drawEntities.push(lineEntity)
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
    drawHandler.setInputAction(() => {
      if (drawPositions.length < 3) { ElMessage.warning('请至少绘制3个顶点'); return }
      
      const polygonEntity = viewer!.entities.add({
        polygon: { hierarchy: new Cesium.PolygonHierarchy(drawPositions), material: Cesium.Color.ORANGE.withAlpha(0.4), outline: true, outlineColor: Cesium.Color.ORANGE }
      })
      drawEntities.push(polygonEntity)
      
      tempDrawingGeometry = toGeoJSONPolygon(drawPositions)
      newDrawing.drawType = 'POLYGON'
      newDrawing.name = `面标绘-${Date.now()}`
      newDrawing.area = calculateArea(drawPositions)
      drawingDialogVisible.value = true
      
      drawHandler?.destroy()
      drawHandler = null
      drawMode.value = null
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
  }
}

const clearDrawing = () => {
  drawHandler?.destroy()
  drawHandler = null
  drawMode.value = null
  drawPositions = []
  drawEntities.forEach(e => viewer?.entities.remove(e))
  drawEntities = []
  tempDrawingGeometry = null
}

const cancelDrawing = () => {
  drawingDialogVisible.value = false
  clearDrawing()
  Object.assign(newDrawing, { name: '', drawType: '', category: 'OTHER', geometry: '', description: '', area: '', length: '' })
}

const saveDrawing = async () => {
  if (!newDrawing.name.trim()) { ElMessage.warning('请输入标绘名称'); return }
  if (!newDrawing.category) { ElMessage.warning('请选择业务分类'); return }
  
  drawingSaving.value = true
  try {
    const payload = {
      name: newDrawing.name,
      drawType: newDrawing.drawType,
      category: newDrawing.category,
      geometry: JSON.stringify(tempDrawingGeometry),
      description: newDrawing.description,
      area: newDrawing.area ? parseFloat(newDrawing.area.replace(/[^0-9.]/g, '')) : null,
      length: newDrawing.length ? parseFloat(newDrawing.length.replace(/[^0-9.]/g, '')) : null
    }
    
    const res = await fetch('/api/map-drawings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${localStorage.getItem('token')}` },
      body: JSON.stringify(payload)
    })
    
    if (!res.ok) throw new Error('保存失败')
    
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success('标绘保存成功')
      drawingDialogVisible.value = false
      Object.assign(newDrawing, { name: '', drawType: '', category: 'OTHER', geometry: '', description: '', area: '', length: '' })
      loadSavedDrawings()
    } else {
      throw new Error(result.message || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    drawingSaving.value = false
  }
}

const loadSavedDrawings = async () => {
  try {
    const res = await fetch('/api/map-drawings', {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    if (!res.ok) return
    const result = await res.json()
    if (result.code === 200) {
      savedDrawings.value = result.data || []
      // 渲染到地图
      savedDrawings.value.forEach(d => renderSavedDrawing(d))
    }
  } catch (e) {
    console.warn('[标绘] 加载失败', e)
  }
}

const deleteDrawing = async (id: number) => {
  try {
    const res = await fetch(`/api/map-drawings/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    if (res.ok) {
      ElMessage.success('删除成功')
      loadSavedDrawings()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const renderSavedDrawing = (d: DrawingData) => {
  if (!viewer || !d.geometry) return
  try {
    const geojson = typeof d.geometry === 'string' ? JSON.parse(d.geometry) : d.geometry
    const coords = geojson.coordinates
    
    if (d.drawType === 'POINT') {
      viewer.entities.add({
        position: Cesium.Cartesian3.fromDegrees(coords[0], coords[1]),
        point: { pixelSize: 10, color: Cesium.Color.CYAN },
        label: { text: d.name, font: '12px sans-serif', fillColor: Cesium.Color.WHITE, pixelOffset: new Cesium.Cartesian2(0, -20) }
      })
    } else if (d.drawType === 'POLYLINE') {
      const positions = coords.map((c: number[]) => Cesium.Cartesian3.fromDegrees(c[0], c[1]))
      viewer.entities.add({ polyline: { positions, width: 3, material: Cesium.Color.CYAN } })
    } else if (d.drawType === 'POLYGON') {
      const positions = coords[0].map((c: number[]) => Cesium.Cartesian3.fromDegrees(c[0], c[1]))
      viewer.entities.add({ polygon: { hierarchy: new Cesium.PolygonHierarchy(positions), material: Cesium.Color.CYAN.withAlpha(0.3), outline: true, outlineColor: Cesium.Color.CYAN } })
    }
  } catch (e) { console.warn('[标绘] 渲染失败', d.id, e) }
}

const flyToDrawing = (d: DrawingData) => {
  if (!viewer || !d.geometry) return
  try {
    const geojson = typeof d.geometry === 'string' ? JSON.parse(d.geometry) : d.geometry
    const coords = geojson.coordinates
    let lon: number, lat: number
    
    if (d.drawType === 'POINT') { lon = coords[0]; lat = coords[1] }
    else if (d.drawType === 'POLYLINE') { lon = coords[0][0]; lat = coords[0][1] }
    else { lon = coords[0][0][0]; lat = coords[0][0][1] }
    
    viewer.camera.flyTo({ destination: Cesium.Cartesian3.fromDegrees(lon, lat, 2000), duration: 1 })
  } catch (e) { console.warn('[标绘] 飞行失败', e) }
}

const getCategoryLabel = (cat: string) => ({ PROJECT: '项目', LAND: '土地', PROPERTY: '房产', OTHER: '其他' }[cat] || cat)
const getCategoryTagType = (cat: string) => ({ PROJECT: 'success', LAND: 'warning', PROPERTY: 'primary', OTHER: 'info' }[cat] || 'info')

// GeoJSON helpers
const toGeoJSONPoint = (cartesian: Cesium.Cartesian3) => {
  const cart = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesian)
  return { type: 'Point', coordinates: [Cesium.Math.toDegrees(cart.longitude), Cesium.Math.toDegrees(cart.latitude)] }
}
const toGeoJSONLineString = (positions: Cesium.Cartesian3[]) => ({
  type: 'LineString',
  coordinates: positions.map(p => { const c = Cesium.Ellipsoid.WGS84.cartesianToCartographic(p); return [Cesium.Math.toDegrees(c.longitude), Cesium.Math.toDegrees(c.latitude)] })
})
const toGeoJSONPolygon = (positions: Cesium.Cartesian3[]) => ({
  type: 'Polygon',
  coordinates: [positions.map(p => { const c = Cesium.Ellipsoid.WGS84.cartesianToCartographic(p); return [Cesium.Math.toDegrees(c.longitude), Cesium.Math.toDegrees(c.latitude)] })]
})


const initCesium = async () => {
  Cesium.Ion.defaultAccessToken = config.cesiumToken
  
  // 1. 初始化
  viewer = new Cesium.Viewer('cesiumContainer', {
    animation: false, timeline: false, baseLayerPicker: false,
    geocoder: false, homeButton: false, sceneModePicker: false,
    navigationHelpButton: false, fullscreenButton: false, 
    selectionIndicator: true, infoBox: false,
    imageryProvider: false
  })
  
  // 2. 默认设置
  try {
    await toggleTerrain(true)
  } catch (e) {
    console.warn('[初始化] 地形加载失败，回退到默认椭球体', e)
    viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider()
  }
  
  // 切换到 ArcGIS 影像 (basemaps[0])，避开天地图问题
  const arcgisMap = basemaps.find(m => m.id === 'arcgis_img')
  if (arcgisMap) await changeBasemap(arcgisMap)
  
  try {
    await toggleBuildings(true)
  } catch (e) {
    console.warn('[初始化] 3D建筑加载失败', e)
  }
  
  // 3. 初始视角
  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(SHIJIAZHUANG.longitude, SHIJIAZHUANG.latitude - 0.05, 5000),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-25),
      roll: 0
    }
  })
  
  // 监听
  viewer.camera.moveEnd.addEventListener(updateCameraInfo)
  
  const handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  handler.setInputAction((movement: any) => {
    // 危险作业申报 — 地图选点模式
    if (dangerPickingMode.value) {
      const cartesian = viewer?.camera.pickEllipsoid(movement.position, viewer.scene.globe.ellipsoid)
      if (!cartesian) return
      const carto = Cesium.Cartographic.fromCartesian(cartesian)
      const lng = Cesium.Math.toDegrees(carto.longitude)
      const lat = Cesium.Math.toDegrees(carto.latitude)

      // 检测是否在片区多边形范围内
      const geomStr = districtDetail.value?.geometry
      if (geomStr) {
        try {
          const geom = typeof geomStr === 'string' ? JSON.parse(geomStr) : geomStr
          if (!pointInPolygon(lng, lat, geom)) {
            ElMessage.warning('所选坐标不在当前片区范围内，请重新选择')
            return // 不退出选点模式，继续等待
          }
        } catch (e) {
          console.warn('片区多边形解析失败，跳过范围检测', e)
        }
      }

      dangerWorkForm.lng = lng
      dangerWorkForm.lat = lat
      dangerPickingMode.value = false

      // 在地图上添加选点标记
      placeDangerPickedMarker(lng, lat)

      // 重新打开申报弹窗
      dangerWorkFormVisible.value = true
      ElMessage.success(`坐标已选择：${lng.toFixed(5)}, ${lat.toFixed(5)}`)
      return
    }

    const picked = viewer?.scene.pick(movement.position)
    if (Cesium.defined(picked) && picked.id) {
      // 危险作业摄像头图标点击
      if ((picked.id as any)._dangerData) {
        onDangerEntityClick(picked.id)
        return
      }
      if (!picked.id.properties) {
        selectedEntity.value = null
        return
      }
      // 跳过项目实体 — 由项目专用点击处理器处理
      const isProject = picked.id.properties._isProject?.getValue()
      if (isProject) return
      
      const props: Record<string, any> = {}
      picked.id.properties.propertyNames.forEach((n: string) => {
        props[n] = picked.id.properties[n]?.getValue()
      })
      selectedEntity.value = { name: picked.id.name || '详细信息', properties: props }
      popupPosition.x = movement.position.x + 10
      popupPosition.y = movement.position.y - 10
    } else {
      selectedEntity.value = null
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
  
  // 加载 GeoJSON 图层
  checkedLayers.value.forEach(async lid => {
    const l = config.layers.find(x => x.id === lid)
    if (l) {
      // 模拟点击事件的数据结构来复用逻辑，或者直接调用加载逻辑
      try {
        const response = await fetch(`/${l.file}`)
        if (!response.ok) return
        const geoJson = await response.json()
        if (geoJson.crs) delete geoJson.crs
        
        const ds = await Cesium.GeoJsonDataSource.load(geoJson, {
          stroke: Cesium.Color.fromCssColorString('#409eff'),
          fill: Cesium.Color.fromCssColorString('rgba(64, 158, 255, 0.3)'),
          strokeWidth: 2
        })
        ds.name = lid
        viewer?.dataSources.add(ds)
      } catch (e) {
        console.error(`[Init] 图层加载失败: ${lid}`, e)
      }
    }
  })
  
  // 加载工具箱数据（书签、标记）
  // 加载项目区域可视化
  loadDictionaries()
  loadProjectAreas()
}

const loadDictionaries = async () => {
  try {
    const { dictApi } = await import('@/api')
    const [catRes, subRes, landSubRes] = await Promise.all([
      dictApi.listItemsByCode('project_category'),
      dictApi.listItemsByCode('engineering_sub_type'),
      dictApi.listItemsByCode('land_sub_type')
    ])
    if (catRes.code === 200) categories.value = catRes.data || []
    if (subRes.code === 200) subTypes.value = subRes.data || []
    if (landSubRes.code === 200) landSubTypes.value = landSubRes.data || []
  } catch (e) {
    console.error('[字典加载] 失败', e)
  }
}

// ========== 项目区域可视化 ==========
const projectDetailVisible = ref(false)
const projectDetail = ref<any>(null)
const projectFiles = ref<any[]>([])
const normalProjectEntities = ref<any[]>([])

// ========== 六大片区 ==========
const districtDetailVisible = ref(false)
const districtDetail = ref<any>(null)
const districtExtraData = ref<any>({})
const districtEntities = ref<any[]>([])

// 字典选项
const categories = ref<any[]>([])
const subTypes = ref<any[]>([])

// 片区颜色配置
const DISTRICT_COLORS: Record<string, { fill: string, stroke: string, label: string }> = {
  '太平河片区': { fill: 'rgba(0, 200, 255, 0.25)', stroke: '#00c8ff', label: '#00d4ff' },
  '留营片区':   { fill: 'rgba(255, 200, 0, 0.25)', stroke: '#ffc800', label: '#ffd740' },
  '龙泉湖片区': { fill: 'rgba(0, 230, 118, 0.25)', stroke: '#00e676', label: '#69f0ae' },
  '和平路片区': { fill: 'rgba(124, 77, 255, 0.25)', stroke: '#7c4dff', label: '#b388ff' },
  '高铁片区':   { fill: 'rgba(255, 82, 82, 0.25)', stroke: '#ff5252', label: '#ff8a80' },
  '东南三环片区': { fill: 'rgba(255, 145, 0, 0.25)', stroke: '#ff9100', label: '#ffab40' }
}

const loadProjectAreas = async () => {
  try {
    const { projectApi } = await import('@/api')
    const res = await projectApi.getAll()
    if (res.code !== 200 || !res.data) return
    
    // 清除旧的实体
    if (viewer) {
      normalProjectEntities.value.forEach(e => viewer!.entities.remove(e))
      districtEntities.value.forEach(e => viewer!.entities.remove(e))
    }
    normalProjectEntities.value = []
    districtEntities.value = []
    
    // 分离片区数据和普通项目
    const normalProjects = res.data.filter((p: any) => p.category !== 'DISTRICT')
    const districtProjects = res.data.filter((p: any) => p.category === 'DISTRICT')
    
    // --------- 渲染普通项目 ---------
    normalProjects.forEach((project: any) => {
      if (!project.geometry) return
      try {
        const geojson = typeof project.geometry === 'string' ? JSON.parse(project.geometry) : project.geometry
        if (!geojson || !geojson.coordinates || !geojson.coordinates[0]) return
        
        const fillMap: Record<string, string> = { LAND: 'rgba(255, 152, 0, 0.35)', ENGINEERING: 'rgba(33, 150, 243, 0.35)' }
        const strokeMap: Record<string, string> = { LAND: '#FF9800', ENGINEERING: '#2196F3' }
        const bgMap: Record<string, Cesium.Color> = {
          LAND: Cesium.Color.fromCssColorString('#E65100'),
          ENGINEERING: Cesium.Color.fromCssColorString('#1565C0')
        }
        const fillColor = fillMap[project.category] || 'rgba(76, 175, 80, 0.35)'
        const strokeColor = strokeMap[project.category] || '#4CAF50'
        const labelBg = bgMap[project.category] || Cesium.Color.fromCssColorString('#2E7D32')
        
        const coords = geojson.coordinates[0]
        const positions = coords.map((c: number[]) => Cesium.Cartesian3.fromDegrees(c[0], c[1]))
        
        const polyEntity = viewer!.entities.add({
          name: project.projectName,
          polygon: {
            hierarchy: new Cesium.PolygonHierarchy(positions),
            material: Cesium.Color.fromCssColorString(fillColor),
            outline: true,
            outlineColor: Cesium.Color.fromCssColorString(strokeColor),
            outlineWidth: 2,
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
          },
          properties: { _projectId: project.id, _isProject: true }
        })
        ;(polyEntity as any)._projectData = project
        normalProjectEntities.value.push(polyEntity)
        
        let sumLon = 0, sumLat = 0
        coords.forEach((c: number[]) => { sumLon += c[0]; sumLat += c[1] })
        const centerLon = sumLon / coords.length
        const centerLat = sumLat / coords.length
        const labelHeight = 300
        
        const lineEnt = viewer!.entities.add({
          polyline: {
            positions: [Cesium.Cartesian3.fromDegrees(centerLon, centerLat, 0), Cesium.Cartesian3.fromDegrees(centerLon, centerLat, labelHeight)],
            width: 1.5,
            material: Cesium.Color.WHITE.withAlpha(0.8)
          }
        })
        normalProjectEntities.value.push(lineEnt)
        
        const labelEnt = viewer!.entities.add({
          position: Cesium.Cartesian3.fromDegrees(centerLon, centerLat, labelHeight),
          label: {
            text: project.projectName,
            font: 'bold 13px sans-serif',
            fillColor: Cesium.Color.WHITE,
            showBackground: true,
            backgroundColor: labelBg.withAlpha(0.85),
            backgroundPadding: new Cesium.Cartesian2(10, 6),
            verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
            horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
            disableDepthTestDistance: Number.POSITIVE_INFINITY,
            pixelOffset: new Cesium.Cartesian2(0, -4)
          },
          properties: { _projectId: project.id, _isProject: true }
        })
        ;(labelEnt as any)._projectData = project
        normalProjectEntities.value.push(labelEnt)
      } catch (e) {
        console.warn('[项目区域] 解析失败:', project.id, e)
      }
    })
    
    // --------- 渲染六大片区（首页显示） ---------
    districtProjects.forEach((project: any) => {
      if (!project.geometry) return
      try {
        const geojson = typeof project.geometry === 'string' ? JSON.parse(project.geometry) : project.geometry
        if (!geojson || !geojson.coordinates) return
        
        const colorCfg = DISTRICT_COLORS[project.projectName] || { fill: 'rgba(0, 150, 255, 0.25)', stroke: '#0096ff', label: '#64b5f6' }
        
        // MultiPolygon: coordinates 是 [[[ring]]] 结构
        const allRings = geojson.type === 'MultiPolygon' ? geojson.coordinates : [geojson.coordinates]
        let allCoords: number[][] = []
        
        allRings.forEach((polygon: number[][][]) => {
          polygon.forEach((ring: number[][]) => {
            const positions = ring.map((c: number[]) => Cesium.Cartesian3.fromDegrees(c[0], c[1]))
            allCoords = allCoords.concat(ring)
            
            const entity = viewer!.entities.add({
              name: project.projectName,
              polygon: {
                hierarchy: new Cesium.PolygonHierarchy(positions),
                material: Cesium.Color.fromCssColorString(colorCfg.fill),
                outline: true,
                outlineColor: Cesium.Color.fromCssColorString(colorCfg.stroke),
                outlineWidth: 3,
                heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
              },
              properties: { _projectId: project.id, _isDistrict: true }
            })
            districtEntities.value.push(entity)
          })
        })
        
        // 计算中心点
        let sumLon = 0, sumLat = 0
        allCoords.forEach((c: number[]) => { sumLon += c[0]; sumLat += c[1] })
        const centerLon = sumLon / allCoords.length
        const centerLat = sumLat / allCoords.length
        const labelHeight = 800
        
        // 垂线
        const lineEntity = viewer!.entities.add({
          polyline: {
            positions: [Cesium.Cartesian3.fromDegrees(centerLon, centerLat, 0), Cesium.Cartesian3.fromDegrees(centerLon, centerLat, labelHeight)],
            width: 2,
            material: Cesium.Color.fromCssColorString(colorCfg.stroke).withAlpha(0.7)
          }
        })
        districtEntities.value.push(lineEntity)
        
        // 彩色标签
        const labelEntity = viewer!.entities.add({
          position: Cesium.Cartesian3.fromDegrees(centerLon, centerLat, labelHeight),
          label: {
            text: project.projectName,
            font: 'bold 15px sans-serif',
            fillColor: Cesium.Color.WHITE,
            showBackground: true,
            backgroundColor: Cesium.Color.fromCssColorString(colorCfg.stroke).withAlpha(0.85),
            backgroundPadding: new Cesium.Cartesian2(14, 8),
            verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
            horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
            disableDepthTestDistance: Number.POSITIVE_INFINITY,
            pixelOffset: new Cesium.Cartesian2(0, -4),
            outlineWidth: 2,
            outlineColor: Cesium.Color.fromCssColorString(colorCfg.stroke),
            style: Cesium.LabelStyle.FILL_AND_OUTLINE
          },
          properties: { _projectId: project.id, _isDistrict: true }
        })
        districtEntities.value.push(labelEntity)
        
        // 附加 color 到 project 对象方便后续使用
        project._districtColor = colorCfg.label
      } catch (e) {
        console.warn('[片区] 解析失败:', project.projectName, e)
      }
    })

    // 点击事件: 点击项目/片区标签 弹出详情
    const projectClickHandler = new Cesium.ScreenSpaceEventHandler(viewer!.scene.canvas)
    projectClickHandler.setInputAction((click: any) => {
      const picked = viewer?.scene.pick(click.position)
      if (Cesium.defined(picked) && picked.id?.properties) {
        // 土地储备模式下，点击地块显示详情
        if (activeModule.value === 'land') {
          const pid = picked.id.properties._projectId?.getValue()
          if (pid) {
            const project = landProjects.value.find((p: any) => p.id === pid)
            if (project) {
              onLandEntityClick(project)
              return
            }
          }
        }

        // 项目建设模式下，点击项目显示详情
        if (activeModule.value === 'project') {
          const pid = picked.id.properties._projectId?.getValue()
          if (pid) {
            const project = engProjects.value.find((p: any) => p.id === pid)
            if (project) {
              onEngEntityClick(project)
              return
            }
          }
        }

        const isDistrict = picked.id.properties._isDistrict?.getValue()
        if (isDistrict) {
          const pid = picked.id.properties._projectId?.getValue()
          if (pid) openDistrictDetail(pid, districtProjects)
          return
        }
        const isProject = picked.id.properties._isProject?.getValue()
        if (isProject) {
          const pid = picked.id.properties._projectId?.getValue()
          if (pid) openProjectDetail(pid)
        }
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    
    console.log('[项目区域] 加载完成, 普通项目', normalProjects.filter((p: any) => p.geometry).length, '个, 片区', districtProjects.length, '个')
    
    // 设置初始可见性：首页只显示片区，隐藏普通项目
    updateEntityVisibility(activeModule.value)
  } catch (e) {
    console.warn('[项目区域] 加载失败', e)
  }
}

// 根据当前模块切换实体可见性
const updateEntityVisibility = (module: string) => {
  const showDistricts = module === 'home'
  const showNormal = module !== 'home'
  
  districtEntities.value.forEach((e: any) => { if (e) e.show = showDistricts })
  normalProjectEntities.value.forEach((e: any) => { if (e) e.show = showNormal })
}

// 监听模块切换
watch(() => activeModule.value, (newVal) => {
  updateEntityVisibility(newVal)
  // 切换时关闭详情弹窗
  districtDetailVisible.value = false
  projectDetailVisible.value = false
})

const openDistrictDetail = async (projectId: number, districtProjects: any[]) => {
  let projData = null
  let found = districtProjects.find((p: any) => p.id === projectId)
  if (found) {
    projData = { ...found }
  } else {
    try {
      const { projectApi } = await import('@/api')
      const res = await projectApi.get(projectId)
      if (res.code === 200) projData = res.data
    } catch (e) {
      console.error('[片区详情] 加载失败', e)
    }
  }

  if (projData) {
    const colorCfg = DISTRICT_COLORS[projData.projectName]
    projData.color = colorCfg?.label || '#00d4ff'
    districtDetail.value = projData
    try {
      districtExtraData.value = projData.extraData ? JSON.parse(projData.extraData) : {}
    } catch {
      districtExtraData.value = {}
    }
    districtDetailVisible.value = true
    projectDetailVisible.value = false
    
    // 加载片区关联的文件列表
    try {
      const { projectFileApi } = await import('@/api')
      const fRes = await projectFileApi.list(projData.id)
      if (fRes.code === 200) projectFiles.value = fRes.data || []
      else projectFiles.value = []
    } catch { 
      projectFiles.value = [] 
    }
  }
}

const saveDistrictDetail = async () => {
  if (!districtDetail.value) return
  try {
    const { projectApi } = await import('@/api')
    districtDetail.value.extraData = JSON.stringify(districtExtraData.value)
    const res = await projectApi.update(districtDetail.value.id, districtDetail.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

const uploadDistrictImage = async (options: any) => {
  if (!districtDetail.value?.id) {
    console.error('[上传附件调试] districtDetail 不存在 id:', districtDetail.value)
    return
  }
  console.log('[上传附件调试] 准备上传图片，接收到的 options 对象:', options)
  console.log('[上传附件调试] 提取到的 file 对象:', options.file, '类型:', Object.prototype.toString.call(options.file))
  try {
    const { projectFileApi } = await import('@/api')
    console.log('[上传附件调试] 开始调用 projectFileApi.upload...')
    const res = await projectFileApi.upload(districtDetail.value.id, options.file)
    console.log('[上传附件调试] 接口返回结果:', res)
    if (res.code === 200 && res.data) {
      ElMessage.success('项目规划图上传成功')
      districtExtraData.value.planningImageUrl = getFileDownloadUrl(res.data.id!)
      await saveDistrictDetail()
    } else {
      console.warn('[上传附件调试] 接口返回业务错误:', res)
    }
  } catch (e: any) {
    console.error('[上传附件调试] 捕获到异常:', e)
    ElMessage.error(`图片上传失败: ${e.message || '未知错误'}`)
  }
}

const uploadDistrictFile = async (options: any) => {
  if (!districtDetail.value?.id) return
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.upload(districtDetail.value.id, options.file)
    if (res.code === 200) {
      ElMessage.success('详细规划上传成功')
      // 更新附件列表
      const fRes = await projectFileApi.list(districtDetail.value.id)
      if (fRes.code === 200) projectFiles.value = fRes.data || []
    }
  } catch (e) {
    ElMessage.error('规划文件上传失败')
  }
}

const viewDistrictFiles = () => {
  if (districtDetail.value?.id) {
    openProjectDetail(districtDetail.value.id)
  }
}

// goToDangerWork 实现在下方危险作业模块区域

const openProjectDetail = async (projectId: number) => {
  try {
    const { projectApi, projectFileApi } = await import('@/api')
    const res = await projectApi.get(projectId)
    if (res.code === 200) {
      projectDetail.value = res.data
      projectDetailVisible.value = true
      districtDetailVisible.value = false
      // 加载文件列表
      try {
        const fRes = await projectFileApi.list(projectId)
        if (fRes.code === 200) projectFiles.value = fRes.data || []
        else projectFiles.value = []
      } catch { projectFiles.value = [] }
    }
  } catch (e) {
    console.error('[项目详情] 加载失败', e)
  }
}

const getFileDownloadUrl = (fileId: number) => {
  return `http://localhost:8080/api/projects/files/${fileId}/download`
}

const uploadProjectFile = async (options: any) => {
  if (!projectDetail.value?.id) return
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.upload(projectDetail.value.id, options.file)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      const fRes = await projectFileApi.list(projectDetail.value.id)
      if (fRes.code === 200) projectFiles.value = fRes.data || []
    }
  } catch (e: any) {
    ElMessage.error('上传失败')
  }
}

const deleteProjectFile = async (fileId: number) => {
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.delete(fileId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      projectFiles.value = projectFiles.value.filter(f => f.id !== fileId)
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// ========== 项目内联编辑 ==========
const projectEditVisible = ref(false)
const projectEditSaving = ref(false)
const projectEditForm = reactive({
  id: 0,
  projectName: '',
  projectCode: '',
  category: '',
  subType: '',
  status: '',
  district: '',
  area: 0,
  totalInvestment: 0,
  startDate: '',
  endDate: '',
  address: '',
  description: '',
  secondaryGroup: '',
  constructionUnit: '',
  developmentUnit: '',
  fundSource: '',
  proceduresStatus: '',
  completionProcedures: '',
  existingProblems: '',
  currentInvestment: 0
})

const editProjectFromMap = () => {
  if (!projectDetail.value?.id) return
  const p = projectDetail.value
  // 复制当前项目数据到编辑表单
  projectEditForm.id = p.id
  projectEditForm.projectName = p.projectName || ''
  projectEditForm.projectCode = p.projectCode || ''
  projectEditForm.category = p.category || ''
  projectEditForm.subType = p.subType || p.projectType || ''
  projectEditForm.status = p.status || ''
  projectEditForm.district = p.district || ''
  projectEditForm.area = Number(p.area || 0) / 666.67
  projectEditForm.totalInvestment = Number(p.totalInvestment || 0)
  projectEditForm.startDate = p.startDate || ''
  projectEditForm.endDate = p.endDate || ''
  projectEditForm.address = p.address || ''
  projectEditForm.description = p.description || ''
  projectEditForm.secondaryGroup = p.secondaryGroup || ''
  projectEditForm.constructionUnit = p.constructionUnit || ''
  projectEditForm.developmentUnit = p.developmentUnit || ''
  projectEditForm.fundSource = p.fundSource || ''
  projectEditForm.proceduresStatus = p.proceduresStatus || ''
  projectEditForm.completionProcedures = p.completionProcedures || ''
  projectEditForm.existingProblems = p.existingProblems || ''
  projectEditForm.currentInvestment = Number(p.currentInvestment || 0)
  projectEditVisible.value = true
}

// ========== 特殊手续附件上传逻辑 ==========
const handleSpecialFileUpload = async (field: 'proceduresStatus' | 'completionProcedures', options: any) => {
  if (!projectEditForm.id) {
    ElMessage.warning('请先保存项目基本信息再上传文件')
    return
  }
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.upload(projectEditForm.id, options.file)
    if (res.code === 200 && res.data) {
      ElMessage.success('PDF 手续文件上传成功')
      const url = getFileDownloadUrl(res.data.id!)
      const currentList = projectEditForm[field] ? projectEditForm[field].split(',').filter(Boolean) : []
      currentList.push(url)
      projectEditForm[field] = currentList.join(',')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error(`附件上传失败: ${e.message || e}`)
  }
}

const removeSpecialFile = (field: 'proceduresStatus' | 'completionProcedures', idx: number) => {
  const currentList = projectEditForm[field] ? projectEditForm[field].split(',').filter(Boolean) : []
  currentList.splice(idx, 1)
  projectEditForm[field] = currentList.join(',')
}

const saveProjectEdit = async () => {
  projectEditSaving.value = true
  try {
    const { projectApi } = await import('@/api')
    const updateData: any = {
      projectName: projectEditForm.projectName,
      projectCode: projectEditForm.projectCode,
      category: projectEditForm.category,
      subType: projectEditForm.subType,
      status: projectEditForm.status,
      district: projectEditForm.district,
      area: projectEditForm.area * 666.67,
      totalInvestment: projectEditForm.totalInvestment,
      startDate: projectEditForm.startDate,
      endDate: projectEditForm.endDate,
      address: projectEditForm.address,
      description: projectEditForm.description,
      secondaryGroup: projectEditForm.secondaryGroup,
      constructionUnit: projectEditForm.constructionUnit,
      developmentUnit: projectEditForm.developmentUnit,
      fundSource: projectEditForm.fundSource,
      proceduresStatus: projectEditForm.proceduresStatus,
      completionProcedures: projectEditForm.completionProcedures,
      existingProblems: projectEditForm.existingProblems,
      currentInvestment: projectEditForm.currentInvestment
    }
    const res = await projectApi.update(projectEditForm.id, updateData)
    if (res.code === 200) {
      ElMessage.success('项目更新成功')
      projectEditVisible.value = false
      // 刷新详情面板 (如果类别被修改，建议直接关闭详情面板)
      projectDetailVisible.value = false
      landDetailVisible.value = false
      engDetailVisible.value = false
      districtDetailVisible.value = false
      
      // 重新加载地图上的区域和对应的列表数据，并应用当前的过滤
      await loadProjectAreas()
      if (activeModule.value === 'land') {
        await loadLandData()
      } else if (activeModule.value === 'project') {
        await loadEngData()
      }
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (e: any) {
    ElMessage.error('保存失败: ' + (e.message || e))
  } finally {
    projectEditSaving.value = false
  }
}

const editDistrictFromMap = () => {
  if (!districtDetail.value?.id) return
  // 复用同一个编辑弹窗
  const p = districtDetail.value
  projectEditForm.id = p.id
  projectEditForm.projectName = p.projectName || ''
  projectEditForm.projectCode = p.projectCode || ''
  projectEditForm.category = p.category || ''
  projectEditForm.subType = p.subType || p.projectType || ''
  projectEditForm.status = p.status || ''
  projectEditForm.district = p.district || ''
  projectEditForm.area = Number(p.area || 0) / 666.67
  projectEditForm.totalInvestment = Number(p.totalInvestment || 0)
  projectEditForm.startDate = p.startDate || ''
  projectEditForm.endDate = p.endDate || ''
  projectEditForm.address = p.address || ''
  projectEditForm.description = p.description || ''
  projectEditVisible.value = true
}

// ========== 文件预览 ==========
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewType = ref('')

const openFilePreview = (file: any) => {
  if (!file.fileName) return
  previewTitle.value = file.fileName
  previewUrl.value = getFileDownloadUrl(file.id)
  
  const ext = file.fileName.toLowerCase().split('.').pop()
  if (['pdf'].includes(ext)) {
    previewType.value = 'pdf'
  } else if (['doc', 'docx'].includes(ext)) {
    previewType.value = 'docx'
  } else if (['xls', 'xlsx'].includes(ext)) {
    previewType.value = 'excel'
  } else if (['ppt', 'pptx'].includes(ext)) {
    previewType.value = 'pptx'
  } else if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext)) {
    previewType.value = 'image'
  } else {
    previewType.value = 'unsupported'
  }
  
  previewVisible.value = true
}

const getFileIcon = (fileName: string) => {
  if (!fileName) return Document
  const ext = fileName.toLowerCase().split('.').pop()
  if (['jpg', 'jpeg', 'png', 'gif'].includes(ext!)) return Picture
  if (['doc', 'docx', 'pdf'].includes(ext!)) return Reading
  if (['xls', 'xlsx'].includes(ext!)) return DataLine
  if (['ppt', 'pptx'].includes(ext!)) return Monitor
  return Files
}

// ========== 土地储备模块: 数据加载 ==========
const loadLandData = async () => {
  try {
    const { projectApi, dictApi } = await import('@/api')

    // 1. 加载土地储备项目
    const res = await projectApi.getByCategory('LAND')
    if (res.code === 200) {
      landProjects.value = res.data || []
    }

    // 2. 加载字典映射 (land_sub_type)
    try {
      const dictRes = await dictApi.listItemsByCode('land_sub_type')
      if (dictRes.code === 200 && dictRes.data) {
        const map: Record<string, string> = {}
        dictRes.data.forEach((item: any) => {
          map[item.itemCode] = item.itemName
        })
        landSubTypeNameMap.value = map
      }
    } catch (e) {
      console.warn('[土地储备] 字典加载失败', e)
    }

    // 3. 前端按 subType 分组统计
    const statsMap: Record<string, { count: number; totalArea: number }> = {}
    landProjects.value.forEach((p: any) => {
      const key = p.subType || 'UNKNOWN'
      if (!statsMap[key]) statsMap[key] = { count: 0, totalArea: 0 }
      statsMap[key].count++
      statsMap[key].totalArea += Number(p.area || 0)
    })
    landSubTypeStats.value = Object.entries(statsMap).map(([subType, data]) => ({
      subType,
      count: data.count,
      totalArea: data.totalArea
    }))

    // 4. 渲染饼图
    setTimeout(() => {
      renderLandPieChart()
    }, 100)

    // 5. 在 Cesium 中只保留土地储备的实体可见
    updateLandEntities()

  } catch (e) {
    console.error('[土地储备] 数据加载失败', e)
  }
}

const renderLandPieChart = () => {
  if (!landPieChartRef.value) return
  if (landPieChart) {
    landPieChart.dispose()
  }
  landPieChart = echarts.init(landPieChartRef.value)

  const pieData = landSubTypeStats.value.map(item => ({
    name: landSubTypeNameMap.value[item.subType] || item.subType || '未分类',
    value: Number((Number(item.totalArea || 0) / 666.67).toFixed(1))
  }))

  const colorPalette = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#00d4ff', '#ffb800']

  landPieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 亩 ({d}%)',
      backgroundColor: 'rgba(10,20,40,0.85)',
      borderColor: 'rgba(0,180,255,0.3)',
      textStyle: { color: '#fff' }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#ccc', fontSize: 12 },
      itemWidth: 14,
      itemHeight: 10
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 4, borderColor: 'rgba(10,20,40,0.8)', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#fff' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' }
      },
      data: pieData,
      color: colorPalette
    }]
  })
}

const updateLandEntities = () => {
  normalProjectEntities.value.forEach((entity: any) => {
    const proj = entity._projectData
    if (!proj) return
    entity.show = proj.category === 'LAND'
  })
}

const showAllEntities = () => {
  normalProjectEntities.value.forEach((entity: any) => {
    entity.show = true
  })
}

const onLandEntityClick = async (project: any) => {
  landDetail.value = project
  landDetailVisible.value = true
  try {
    const { projectFileApi } = await import('@/api')
    const fRes = await projectFileApi.list(project.id)
    if (fRes.code === 200) landFiles.value = fRes.data || []
    else landFiles.value = []
  } catch { landFiles.value = [] }
}

const editLandFromMap = () => {
  if (!landDetail.value?.id) return
  const p = landDetail.value
  projectEditForm.id = p.id
  projectEditForm.projectName = p.projectName || ''
  projectEditForm.projectCode = p.projectCode || ''
  projectEditForm.category = p.category || ''
  projectEditForm.subType = p.subType || p.projectType || ''
  projectEditForm.status = p.status || ''
  projectEditForm.district = p.district || ''
  projectEditForm.area = Number(p.area || 0) / 666.67
  projectEditForm.totalInvestment = Number(p.totalInvestment || 0)
  projectEditForm.startDate = p.startDate || ''
  projectEditForm.endDate = p.endDate || ''
  projectEditForm.address = p.address || ''
  projectEditForm.description = p.description || ''
  projectEditVisible.value = true
}

const uploadLandFile = async (options: any) => {
  if (!landDetail.value?.id) return
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.upload(landDetail.value.id, options.file)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      const fRes = await projectFileApi.list(landDetail.value.id)
      if (fRes.code === 200) landFiles.value = fRes.data || []
    }
  } catch (e: any) { ElMessage.error('上传失败') }
}

const deleteLandFile = async (fileId: number) => {
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.delete(fileId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (landDetail.value?.id) {
        const fRes = await projectFileApi.list(landDetail.value.id)
        if (fRes.code === 200) landFiles.value = fRes.data || []
      }
    }
  } catch (e: any) { ElMessage.error('删除失败') }
}

// ========== 项目建设模块: 状态 ==========
const engProjects = ref<any[]>([])
const engDetailVisible = ref(false)
const engDetail = ref<any>(null)
const engFiles = ref<any[]>([])
const engBarChartRef = ref<HTMLElement | null>(null)
const engDetailChartRef = ref<HTMLElement | null>(null)
let engBarChart: echarts.ECharts | null = null
let engDetailChart: echarts.ECharts | null = null
const engSubTypeNameMap = ref<Record<string, string>>({})
const engSubTypeList = ref<any[]>([])
const engActiveSubType = ref<string>('')
const companyGroups = ref<any[]>([])

const engTotalInvestment = computed(() => {
  return engProjects.value.reduce((sum: number, p: any) => sum + Number(p.totalInvestment || 0), 0)
})

const engSubCount = (subType: string) => {
  return engProjects.value.filter((p: any) => p.subType === subType).length
}

const engStatusCount = (status: string) => {
  return engProjects.value.filter((p: any) => p.status === status).length
}

// 加载工程项目数据
const loadEngData = async () => {
  try {
    const { projectApi, dictApi } = await import('@/api')

    // 1. 加载工程项目
    const res = await projectApi.getByCategory('ENGINEERING')
    if (res.code === 200) {
      engProjects.value = res.data || []
    }

    // 2. 加载字典映射 (engineering_sub_type)
    try {
      const dictRes = await dictApi.listItemsByCode('engineering_sub_type')
      if (dictRes.code === 200 && dictRes.data) {
        // 过滤掉“片区规划”，因为现在被移到了外面单独展示
        engSubTypeList.value = dictRes.data.filter((item: any) => item.itemName !== '片区规划' && item.itemCode !== 'PIANQUGUIHUA')
        const map: Record<string, string> = {}
        dictRes.data.forEach((item: any) => {
          map[item.itemCode] = item.itemName
        })
        engSubTypeNameMap.value = map
      }
    } catch (e) {
      console.warn('[项目建设] 字典加载失败', e)
    }

    // 3. 加载公司集团字典
    try {
      const cgRes = await dictApi.listItemsByCode('company_group')
      if (cgRes.code === 200) companyGroups.value = cgRes.data || []
    } catch (e) {
      console.warn('[项目建设] 公司集团字典加载失败', e)
    }

    // 4. 渲染柱状图
    setTimeout(() => { renderEngBarChart() }, 100)

    // 5. 仅显示工程项目实体
    updateEngEntities()

  } catch (e) {
    console.error('[项目建设] 数据加载失败', e)
  }
}

// 渲染状态柱状图
const renderEngBarChart = () => {
  if (!engBarChartRef.value) return
  if (engBarChart) { engBarChart.dispose() }
  engBarChart = echarts.init(engBarChartRef.value)

  const statusList = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
  const statusNames = ['待启动', '进行中', '已完成', '已取消']
  const statusColors = ['#409eff', '#ffb800', '#67c23a', '#909399']

  const counts = statusList.map(s => engStatusCount(s))

  engBarChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 30, right: 10, top: 10, bottom: 30 },
    xAxis: {
      type: 'category',
      data: statusNames,
      axisLabel: { color: '#8899aa' },
      axisLine: { lineStyle: { color: 'rgba(0,229,255,0.3)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#8899aa' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
    },
    series: [{
      type: 'bar',
      data: counts.map((v, i) => ({ value: v, itemStyle: { color: statusColors[i] } })),
      barWidth: '40%'
    }]
  })
}

// 渲染详情面板投资额柱状图
const renderEngDetailChart = () => {
  if (!engDetailChartRef.value || !engDetail.value) return
  if (engDetailChart) { engDetailChart.dispose() }
  engDetailChart = echarts.init(engDetailChartRef.value)

  const p = engDetail.value
  const totalInv = Number(p.totalInvestment || 0)
  const currentInv = Number(p.currentInvestment || 0)
  // 动态获取年份
  const createdYear = p.createdAt ? new Date(p.createdAt).getFullYear() : new Date().getFullYear()

  engDetailChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c} 万元' },
    legend: { textStyle: { color: '#ccc', fontSize: 10 }, top: 5 },
    grid: { left: 50, right: 10, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['项目总投资', '截至目前投资', `${createdYear}年投资`],
      axisLabel: { color: '#8899aa', fontSize: 10 },
      axisLine: { lineStyle: { color: 'rgba(0,229,255,0.3)' } }
    },
    yAxis: {
      type: 'value',
      name: '(万元)',
      nameTextStyle: { color: '#8899aa' },
      axisLabel: { color: '#8899aa' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
    },
    series: [{
      type: 'bar',
      barWidth: '40%',
      data: [
        { value: totalInv, itemStyle: { color: '#409eff' } },
        { value: currentInv, itemStyle: { color: '#ffb800' } },
        { value: totalInv, itemStyle: { color: '#e6a23c' } }
      ]
    }]
  })
}

// 仅显示工程项目实体
const updateEngEntities = () => {
  normalProjectEntities.value.forEach((entity: any) => {
    const proj = entity._projectData
    if (!proj) return
    if (engActiveSubType.value) {
      entity.show = proj.category === 'ENGINEERING' && (proj.subType === engActiveSubType.value || proj.projectType === engActiveSubType.value)
    } else {
      entity.show = proj.category === 'ENGINEERING'
    }
  })
}

// 底部子类过滤切换
const toggleEngSubType = (code: string) => {
  engActiveSubType.value = engActiveSubType.value === code ? '' : code
  updateEngEntities()
}

// 点击工程项目实体
const onEngEntityClick = async (project: any) => {
  engDetail.value = project
  engDetailVisible.value = true
  try {
    const { projectFileApi } = await import('@/api')
    const fRes = await projectFileApi.list(project.id)
    if (fRes.code === 200) engFiles.value = fRes.data || []
    else engFiles.value = []
  } catch { engFiles.value = [] }
  setTimeout(() => { renderEngDetailChart() }, 100)
}

// 编辑工程项目
const editEngFromMap = () => {
  if (!engDetail.value?.id) return
  const p = engDetail.value
  projectEditForm.id = p.id
  projectEditForm.projectName = p.projectName || ''
  projectEditForm.projectCode = p.projectCode || ''
  projectEditForm.category = p.category || ''
  projectEditForm.subType = p.subType || p.projectType || ''
  projectEditForm.status = p.status || ''
  projectEditForm.district = p.district || ''
  projectEditForm.area = Number(p.area || 0) / 666.67
  projectEditForm.totalInvestment = Number(p.totalInvestment || 0)
  projectEditForm.startDate = p.startDate || ''
  projectEditForm.endDate = p.endDate || ''
  projectEditForm.address = p.address || ''
  projectEditForm.description = p.description || ''
  projectEditVisible.value = true
}

// 上传附件
const uploadEngFile = async (options: any) => {
  if (!engDetail.value?.id) return
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.upload(engDetail.value.id, options.file)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      const fRes = await projectFileApi.list(engDetail.value.id)
      if (fRes.code === 200) engFiles.value = fRes.data || []
    }
  } catch (e: any) { ElMessage.error('上传失败') }
}

// 删除附件
const deleteEngFile = async (fileId: number) => {
  try {
    const { projectFileApi } = await import('@/api')
    const res = await projectFileApi.delete(fileId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (engDetail.value?.id) {
        const fRes = await projectFileApi.list(engDetail.value.id)
        if (fRes.code === 200) engFiles.value = fRes.data || []
      }
    }
  } catch (e: any) { ElMessage.error('删除失败') }
}

// ========== 模块切换监听 ==========
watch(activeModule, async (newVal, oldVal) => {
  if (newVal === 'land') {
    loadLandData()
  } else if (oldVal === 'land') {
    showAllEntities()
    landDetailVisible.value = false
  }
  if (newVal === 'project') {
    loadEngData()
  } else if (oldVal === 'project') {
    showAllEntities()
    engDetailVisible.value = false
    engActiveSubType.value = ''
  }
  if (newVal === 'danger') {
    // 危险作业模块：隐藏其他实体，只显示监控点
    hideDangerIrrelevantEntities()
    await loadDangerData()
    await loadDangerMonitors()
  } else if (oldVal === 'danger') {
    // 离开危险模块：恢复所有实体显示并清除监控点
    dangerMonitorEntities.value.forEach(e => viewer?.entities.remove(e))
    dangerMonitorEntities.value = []
    clearDangerPickedMarker()
    showAllEntities()
  }
})

/** 进入危险作业模块时隐藏非危险相关实体 */
const hideDangerIrrelevantEntities = () => {
  if (!viewer) return
  viewer.entities.values.forEach((entity: any) => {
    // 保留危险作业监控点（含 _dangerData）和选点标记
    if (entity._dangerData || entity === dangerPickedMarker.value) return
    entity.show = false
  })
}

// ============================================================
// ===== 危险作业模块 ===========================================
// ============================================================

// --- 危险作业申报弹窗状态 ---
const dangerWorkFormVisible = ref(false)
const dangerWorkSubmitting = ref(false)
const dangerPickingMode = ref(false)
const dangerWorkFormMeasures = ref<string[]>([])
const dangerWorkForm = reactive<{
  districtName?: string
  districtProjectId?: number
  workType?: string
  workName?: string
  riskLevel?: string
  applicant?: string
  responsiblePerson?: string
  implementor?: string
  locationDesc?: string
  lng?: number
  lat?: number
  planStartTime?: string
  planEndTime?: string
  workDescription?: string
}>({
  districtName: '',
  riskLevel: '一般风险',
})

// ===== 危险作业地图选点辅助函数 =====

/**
 * 射线法判断点是否在 GeoJSON Polygon/MultiPolygon 内
 */
const pointInPolygon = (lng: number, lat: number, geometry: any): boolean => {
  const testRing = (ring: number[][]): boolean => {
    let inside = false
    for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
      const xi = ring[i][0], yi = ring[i][1]
      const xj = ring[j][0], yj = ring[j][1]
      const intersect = ((yi > lat) !== (yj > lat)) &&
        (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)
      if (intersect) inside = !inside
    }
    return inside
  }

  const testPolygon = (coords: number[][][]): boolean => {
    // coords[0] 是外轮坤，先判断在外轮坤内，再排除洞
    if (!testRing(coords[0])) return false
    for (let i = 1; i < coords.length; i++) {
      if (testRing(coords[i])) return false // 在洞里面
    }
    return true
  }

  if (geometry.type === 'Polygon') {
    return testPolygon(geometry.coordinates)
  } else if (geometry.type === 'MultiPolygon') {
    return geometry.coordinates.some((poly: number[][][]) => testPolygon(poly))
  } else if (geometry.type === 'GeometryCollection') {
    return geometry.geometries.some((g: any) => pointInPolygon(lng, lat, g))
  }
  return true // 未知类型不限制
}

// 地图选点标记实体
const dangerPickedMarker = ref<any>(null)

const clearDangerPickedMarker = () => {
  if (dangerPickedMarker.value && viewer) {
    viewer.entities.remove(dangerPickedMarker.value)
    dangerPickedMarker.value = null
  }
}

const placeDangerPickedMarker = (lng: number, lat: number) => {
  clearDangerPickedMarker()
  if (!viewer) return
  dangerPickedMarker.value = viewer.entities.add({
    position: Cesium.Cartesian3.fromDegrees(lng, lat, 10),
    point: {
      pixelSize: 14,
      color: Cesium.Color.fromCssColorString('#F56C6C'),
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
      disableDepthTestDistance: Number.POSITIVE_INFINITY,
    },
    label: {
      text: `危险作业选点`,
      font: 'bold 13px sans-serif',
      fillColor: Cesium.Color.WHITE,
      outlineColor: Cesium.Color.BLACK,
      outlineWidth: 2,
      style: Cesium.LabelStyle.FILL_AND_OUTLINE,
      pixelOffset: new Cesium.Cartesian2(0, -24),
      disableDepthTestDistance: Number.POSITIVE_INFINITY,
      showBackground: true,
      backgroundColor: Cesium.Color.fromCssColorString('rgba(245,108,108,0.8)'),
    },
  })
}

// 打开危险作业申报弹窗时清除上次选点标记
const openDangerWorkForm = () => {
  clearDangerPickedMarker()
  Object.assign(dangerWorkForm, {
    districtName: districtDetail.value?.projectName || '',
    districtProjectId: districtDetail.value?.id,
    workType: '',
    workName: '',
    riskLevel: '一般风险',
    applicant: '',
    responsiblePerson: '',
    implementor: '',
    locationDesc: '',
    lng: undefined,
    lat: undefined,
    planStartTime: '',
    planEndTime: '',
    workDescription: '',
  })
  dangerWorkFormMeasures.value = []
  dangerPickingMode.value = false
  dangerWorkFormVisible.value = true
}

// 开始选点（关闭弹窗让出地图）
const startDangerPicking = () => {
  dangerPickingMode.value = true
  dangerWorkFormVisible.value = false // 关闭弹窗，让地图可点击
  ElMessage.info('请在片区范围内单击选择危险作业位置')
}

// 提交申报
const submitDangerWork = async () => {
  if (!dangerWorkForm.workType || !dangerWorkForm.workName) {
    ElMessage.warning('请填写作业类型和作业名称')
    return
  }
  if (!dangerWorkForm.lng || !dangerWorkForm.lat) {
    ElMessage.warning('请在地图上选择坐标点')
    return
  }
  dangerWorkSubmitting.value = true
  try {
    const { dangerousWorkApi } = await import('@/api')
    const payload = {
      ...dangerWorkForm,
      safetyMeasures: dangerWorkFormMeasures.value.join(','),
    }
    const res = await dangerousWorkApi.apply(payload)
    if (res.code === 200) {
      ElMessage.success(`申报成功！监控点编号：${res.data?.monitorCode}`)
      dangerWorkFormVisible.value = false
      await loadDangerMonitors()
      await loadDangerData()
    } else {
      ElMessage.error(res.message || '申报失败')
    }
  } catch (e) {
    ElMessage.error('申报失败，请检查网络')
  } finally {
    dangerWorkSubmitting.value = false
  }
}

// --- 危险作业模块地图图标实体 ---
const dangerMonitorEntities = ref<any[]>([])

const CAMERA_SVG = `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 40 40'>
  <circle cx='20' cy='20' r='18' fill='rgba(0,0,0,0.6)' stroke='#00d4ff' stroke-width='2'/>
  <rect x='8' y='14' width='16' height='12' rx='3' fill='#00d4ff'/>
  <polygon points='24,16 32,12 32,28 24,24' fill='#00d4ff'/>
  <circle cx='16' cy='20' r='3' fill='#001529'/>
</svg>`

const CAMERA_BLOB = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(CAMERA_SVG)}`

const loadDangerMonitors = async () => {
  // 清除旧图标
  dangerMonitorEntities.value.forEach(e => viewer?.entities.remove(e))
  dangerMonitorEntities.value = []
  if (!viewer) return
  try {
    const { dangerousWorkApi } = await import('@/api')
    const res = await dangerousWorkApi.getMonitors()
    if (res.code !== 200) return
    const monitors = res.data || []
    const now = new Date()
    monitors.forEach((m: any) => {
      if (!m.lng || !m.lat) return
      // 计划结束时间已过则跳过不显示
      if (m.planEndTime && new Date(m.planEndTime) < now) return
      const entity = viewer!.entities.add({
        position: Cesium.Cartesian3.fromDegrees(m.lng, m.lat, 50),
        billboard: {
          image: CAMERA_BLOB,
          width: 40,
          height: 40,
          verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
        label: {
          text: m.monitorCode || '',
          font: 'bold 13px sans-serif',
          fillColor: Cesium.Color.fromCssColorString('#00d4ff'),
          outlineColor: Cesium.Color.BLACK,
          outlineWidth: 2,
          style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          pixelOffset: new Cesium.Cartesian2(0, -48),
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
          showBackground: true,
          backgroundColor: Cesium.Color.fromCssColorString('rgba(0,0,0,0.6)'),
        },
        _dangerData: m,
      } as any)
      dangerMonitorEntities.value.push(entity)
    })
  } catch (e) {
    console.error('加载危险作业监控点失败', e)
  }
}

// --- 危险作业大屏统计数据 ---
const dangerStats = reactive<Record<string, any>>({ total: 0, inProgress: 0, completed: 0, cancelled: 0 })
const dangerRecentRecords = ref<any[]>([])

const dangerRiskChart = ref<HTMLElement | null>(null)
const dangerTypeChart = ref<HTMLElement | null>(null)
const dangerTrendChart = ref<HTMLElement | null>(null)
let dangerRiskChartInst: any = null
let dangerTypeChartInst: any = null
let dangerTrendChartInst: any = null

const loadDangerData = async () => {
  try {
    const { dangerousWorkApi } = await import('@/api')
    const res = await dangerousWorkApi.getStatistics()
    if (res.code !== 200) return
    const data = res.data || {}
    Object.assign(dangerStats, {
      total: data.total || 0,
      inProgress: data.inProgress || 0,
      completed: data.completed || 0,
      cancelled: data.cancelled || 0,
    })
    dangerRecentRecords.value = data.recentRecords || []

    // 延迟渲染图表
    setTimeout(() => {
      renderDangerRiskChart(data.riskStats || [])
      renderDangerTypeChart(data.typeStats || [])
      renderDangerTrendChart(data.monthlyTrend || [])
    }, 200)
  } catch (e) {
    console.error('加载危险作业统计失败', e)
  }
}

const renderDangerRiskChart = (riskData: any[]) => {
  if (!dangerRiskChart.value) return
  if (dangerRiskChartInst) dangerRiskChartInst.dispose()
  dangerRiskChartInst = echarts.init(dangerRiskChart.value)
  // PostgreSQL 将别名转为全小写：risklevel, cnt
  const seriesData = riskData.length > 0
    ? riskData.map((r: any) => ({
        name: r.risklevel || r.riskLevel || '未知',
        value: Number(r.cnt || 0)
      }))
    : [
        { name: '重大风险', value: 4, itemStyle: { color: '#F56C6C' } },
        { name: '较大风险', value: 7, itemStyle: { color: '#E6A23C' } },
        { name: '一般风险', value: 7, itemStyle: { color: '#67C23A' } },
      ]
  dangerRiskChartInst.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)', textStyle: { color: '#fff' }, backgroundColor: 'rgba(10,20,40,0.85)' },
    legend: { bottom: 4, textStyle: { color: '#ccc', fontSize: 11 }, itemWidth: 12, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['42%', '65%'],
      center: ['50%', '42%'],
      data: seriesData,
      itemStyle: { borderRadius: 4 },
      label: {
        show: true,
        color: '#ccc',
        fontSize: 11,
        formatter: '{b}\n{d}%'
      },
      labelLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
    }],
  })
}

const renderDangerTypeChart = (typeStats: any[]) => {
  if (!dangerTypeChart.value) return
  if (dangerTypeChartInst) dangerTypeChartInst.dispose()
  dangerTypeChartInst = echarts.init(dangerTypeChart.value)
  const types = ['高空作业', '受限空间作业', '危化品储存', '起重作业', '明火作业']
  const inProgressData = types.map(t => {
    // PostgreSQL 返回 worktype（全小写）或 workType，同时兼容两种
    const r = typeStats.find((s: any) =>
      (s.worktype === t || s.workType === t) && (s.status === 'IN_PROGRESS')
    )
    return r ? Number(r.cnt || 0) : 0
  })
  const completedData = types.map(t => {
    const r = typeStats.find((s: any) =>
      (s.worktype === t || s.workType === t) && (s.status === 'COMPLETED')
    )
    return r ? Number(r.cnt || 0) : 0
  })
  dangerTypeChartInst.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', textStyle: { color: '#fff' }, backgroundColor: 'rgba(10,20,40,0.85)' },
    legend: { data: ['进行中', '已完成'], textStyle: { color: '#ccc', fontSize: 10 }, top: 0 },
    grid: { top: 25, bottom: 30, left: 35, right: 8 },
    xAxis: { type: 'category', data: ['高空', '受限空间', '危化品', '起重', '明火'], axisLabel: { color: '#aaa', fontSize: 10 } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#aaa', fontSize: 10 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
    series: [
      { name: '进行中', type: 'bar', stack: 'total', data: inProgressData, itemStyle: { color: '#E6A23C' }, barMaxWidth: 30 },
      { name: '已完成', type: 'bar', stack: 'total', data: completedData, itemStyle: { color: '#67C23A' }, barMaxWidth: 30 },
    ],
  })
}

const renderDangerTrendChart = (trendData: any[]) => {
  if (!dangerTrendChart.value) return
  if (dangerTrendChartInst) dangerTrendChartInst.dispose()
  dangerTrendChartInst = echarts.init(dangerTrendChart.value)
  const months = trendData.length > 0 ? trendData.map((t: any) => t.month) :
    ['2025-11', '2025-12', '2026-01', '2026-02', '2026-03', '2026-04']
  const totals    = trendData.length > 0 ? trendData.map((t: any) => Number(t.total     || 0)) : [2, 3, 3, 3, 2, 5]
  const completed  = trendData.length > 0 ? trendData.map((t: any) => Number(t.completed || 0)) : [2, 3, 3, 3, 2, 0]
  // PostgreSQL 返回 highrisk（小写）或 highRisk，兼容两种
  const highRisk   = trendData.length > 0 ? trendData.map((t: any) => Number(t.highrisk  ?? t.highRisk ?? 0)) : [1, 2, 1, 1, 1, 2]
  dangerTrendChartInst.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', textStyle: { color: '#fff' }, backgroundColor: 'rgba(10,20,40,0.85)' },
    legend: { data: ['申报数', '完成数', '风险数'], textStyle: { color: '#ccc', fontSize: 10 }, top: 0 },
    grid: { top: 25, bottom: 35, left: 35, right: 8 },
    xAxis: { type: 'category', data: months, axisLabel: { color: '#aaa', fontSize: 9, rotate: 20 } },
    yAxis: { type: 'value', axisLabel: { color: '#aaa', fontSize: 10 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
    series: [
      { name: '申报数', type: 'bar', data: totals, itemStyle: { color: '#409EFF' }, barMaxWidth: 20 },
      { name: '完成数', type: 'line', data: completed, smooth: true, symbol: 'circle', symbolSize: 5, itemStyle: { color: '#67C23A' } },
      { name: '风险数', type: 'line', data: highRisk, smooth: true, symbol: 'diamond', symbolSize: 5, itemStyle: { color: '#F56C6C' } },
    ],
  })
}

// --- 危险作业历史记录（管理员）---
const dangerHistoryVisible = ref(false)
const dangerHistoryList = ref<any[]>([])
const dangerHistoryLoading = ref(false)

const openDangerHistory = async () => {
  dangerHistoryVisible.value = true
  dangerHistoryLoading.value = true
  try {
    const { dangerousWorkApi } = await import('@/api')
    const res = await dangerousWorkApi.list()
    if (res.code === 200) dangerHistoryList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载历史记录失败')
  } finally {
    dangerHistoryLoading.value = false
  }
}

// --- 监控详情弹窗 ---
const dangerMonitorVisible = ref(false)
const dangerMonitorDetail = ref<any>(null)
const webcamVideoRef = ref<HTMLVideoElement | null>(null)
let webcamStream: MediaStream | null = null
let mediaRecorder: MediaRecorder | null = null
let recordedChunks: Blob[] = []
const isRecording = ref(false)

const openDangerMonitor = async (item: any) => {
  dangerMonitorDetail.value = item
  dangerMonitorVisible.value = true
  // 等待 DOM 就绪后启动摄像头
  setTimeout(() => startWebcam(), 300)
}

const startWebcam = async () => {
  try {
    webcamStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
    if (webcamVideoRef.value) {
      webcamVideoRef.value.srcObject = webcamStream
    }
  } catch (e) {
    ElMessage.warning('无法访问摄像头，请检查权限')
  }
}

const stopWebcam = () => {
  if (isRecording.value) stopRecording()
  webcamStream?.getTracks().forEach(t => t.stop())
  webcamStream = null
  if (webcamVideoRef.value) webcamVideoRef.value.srcObject = null
}

const startRecording = () => {
  if (!webcamStream) { ElMessage.warning('摄像头未就绪'); return }
  recordedChunks = []
  mediaRecorder = new MediaRecorder(webcamStream, { mimeType: 'video/webm' })
  mediaRecorder.ondataavailable = (e) => { if (e.data.size > 0) recordedChunks.push(e.data) }
  mediaRecorder.onstop = () => {
    const blob = new Blob(recordedChunks, { type: 'video/webm' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `monitor_${dangerMonitorDetail.value?.monitorCode || 'record'}_${Date.now()}.webm`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('录制视频已保存')
  }
  mediaRecorder.start()
  isRecording.value = true
  ElMessage.success('开始录制...')
}

const stopRecording = () => {
  mediaRecorder?.stop()
  isRecording.value = false
}

// 点击危险作业地图实体（由 Cesium click 事件分发）
const onDangerEntityClick = (entity: any) => {
  const data = (entity as any)._dangerData
  if (data) openDangerMonitor(data)
}

// 危险作业模块的 watch 已合并到上方统一 watch(activeModule) 块中

// ============================================================

onMounted(() => {
  // 确保 Token 存在
  if (!config.cesiumToken) {
    ElMessage.error('Cesium Token 未配置，无法加载 3D 数据')
    return
  }
  initCesium()
})
onUnmounted(() => { viewer?.destroy(); viewer = null })
</script>

<style scoped lang="scss">
.user-map-view {
  width: 100vw;
  height: 100vh;
  position: relative;
  background: #000;
  overflow: hidden;
  font-family: "Microsoft YaHei", sans-serif;
}

.cesium-container {
  width: 100%;
  height: 100%;
}

/* 1. Header */
.mars-header {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 70px;
  background: linear-gradient(to bottom, rgba(0, 21, 41, 0.9) 0%, rgba(0, 21, 41, 0.6) 50%, rgba(0, 0, 0, 0) 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 100;
  pointer-events: none;
  
  .logo-area {
    pointer-events: auto;
    display: flex;
    align-items: center;
    gap: 10px;
    color: #fff;
    
    .logo-icon {
      font-size: 28px;
      color: #00ffff;
    }
    
    .system-name {
      font-size: 22px;
      font-weight: 700;
      letter-spacing: 1px;
      text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
      background: linear-gradient(to bottom, #fff, #bde6ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
  
  .nav-menu {
    pointer-events: auto;
    display: flex;
    gap: 5px;
    
    .nav-item {
      padding: 8px 16px;
      color: rgba(255, 255, 255, 0.8);
      cursor: pointer;
      border-radius: 4px;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.3s;
      
      &:hover, &.active {
        color: #00ffff;
        background: rgba(0, 255, 255, 0.15);
      }
      
      .el-icon { font-size: 18px; }
      span { font-size: 15px; font-weight: 500; }
    }
  }
  
  .user-area {
    pointer-events: auto;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      
      .user-name { color: #fff; font-size: 14px; }
      .user-avatar { border: 2px solid rgba(0, 255, 255, 0.5); }
    }
  }
}

/* 2. Search Panel */
.mars-search-panel {
  position: absolute;
  top: 80px;
  left: 20px;
  width: 340px;
  z-index: 100;
  
  .search-box {
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
    
    :deep(.mars-input) {
      .el-input__wrapper {
        background: rgba(23, 31, 46, 0.9);
        box-shadow: none;
        border: 1px solid rgba(128, 128, 128, 0.3);
      }
      .el-input__inner { color: #fff; }
      .el-input-group__append {
        background: #409eff;
        border: none;
        color: #fff;
        
        &:hover { background: #66b1ff; }
      }
    }
  }
  
  .search-results {
    margin-top: 10px;
    background: rgba(23, 31, 46, 0.9);
    border-radius: 4px;
    border: 1px solid rgba(128, 128, 128, 0.3);
    color: #fff;
    padding: 5px 0;
    
    .result-item {
      padding: 10px 15px;
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      
      &:hover {
        background: rgba(0, 255, 255, 0.1);
        color: #00ffff;
      }
      
      .el-icon { font-size: 16px; color: #aaa; }
    }
  }
}

/* 3. Top Toolbar */
.mars-toolbar-top {
  position: absolute;
  top: 80px;
  right: 20px;
  display: flex;
  gap: 10px;
  z-index: 100;
  
  .tool-btn {
    background: rgba(23, 31, 46, 0.8);
    border: 1px solid rgba(255, 255, 255, 0.15);
    color: #fff;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    transition: all 0.3s;
    
    &:hover, &.active {
      background: #409eff;
      border-color: #409eff;
    }
  }
}

/* 4. Right Panel (Layers) */
.mars-panel {
  position: absolute;
  top: 130px;
  right: 20px;
  width: 280px;
  background: rgba(23, 31, 46, 0.9);
  border: 1px solid rgba(0, 180, 255, 0.3);
  border-radius: 4px;
  color: #fff;
  z-index: 99;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 200px);
  padding-bottom: 5px;
  
  .panel-header {
    height: 40px;
    background: rgba(0, 180, 255, 0.15);
    border-bottom: 1px solid rgba(0, 180, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 15px;
    font-weight: 600;
    font-size: 15px;
    
    .close-btn { cursor: pointer; &:hover { color: #f56c6c; } }
  }
  
  .panel-content {
    padding: 10px;
    overflow-y: auto;
    
    :deep(.mars-tree) {
      background: transparent;
      color: #fff;
      
      .el-tree-node__content {
        &:hover { background: rgba(255, 255, 255, 0.05); }
      }
      
      .el-tree-node:focus > .el-tree-node__content {
        background: rgba(255, 255, 255, 0.1);
      }
      
      .custom-tree-node {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  }
  
  .opacity-control {
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    
    .label { font-size: 13px; color: #aaa; margin-bottom: 5px; display: block; }
  }
}

/* Basemap Grid */
.basemap-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 10px;
}

.basemap-item {
  cursor: pointer;
  text-align: center;
  padding: 5px;
  border-radius: 4px;
  border: 1px solid transparent;
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  
  &.active {
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.2);
    
    .map-name { color: #409eff; }
  }
  
  .map-icon {
    width: 100%;
    height: 60px;
    border-radius: 4px;
    margin-bottom: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .basemap-emoji {
      font-size: 24px;
      filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
    }
  }
  
  .map-name {
    font-size: 12px;
    color: #ccc;
  }
}

.control-group {
  grid-column: 1 / -1;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 10px;
  padding-top: 15px;
  
  .control-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-size: 14px;
    color: #fff;
  }
}

/* 5. Side Toolbar */
.mars-toolbar-side {
  position: absolute;
  bottom: 50px;
  left: 20px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  z-index: 100;
  
  .side-btn {
    width: 36px;
    height: 36px;
    background: rgba(23, 31, 46, 0.8);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #409eff;
      border-color: #409eff;
    }
  }
}

/* 6. Statusbar */
.mars-statusbar {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 28px;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
  font-size: 12px;
  color: #ccc;
  z-index: 100;
  backdrop-filter: blur(2px);
  
  .status-item { min-width: 100px; }
  .status-copyright { margin-left: auto; color: #666; }
}

/* 7. Popup */
.mars-popup {
  position: absolute;
  width: 280px;
  background: rgba(23, 31, 46, 0.95);
  border: 1px solid #00b4ff;
  border-radius: 4px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.5);
  color: #fff;
  z-index: 200;
  transform: translate(10px, -50%);
  pointer-events: none;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: -6px;
    margin-top: -6px;
    border-right: 6px solid #00b4ff;
    border-top: 6px solid transparent;
    border-bottom: 6px solid transparent;
  }
  
  .popup-header {
    height: 36px;
    background: rgba(0, 180, 255, 0.2);
    border-bottom: 1px solid rgba(0, 180, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px;
    font-weight: 600;
    font-size: 14px;
  }
  
  .popup-content {
    padding: 12px;
    
    .prop-item {
      display: flex;
      font-size: 13px;
      margin-bottom: 6px;
      
      .prop-label { color: #aaa; width: 80px; }
      .prop-value { color: #fff; flex: 1; }
    }
  }
}

/* Project Detail Panel */
.project-detail-panel {
  position: absolute;
  top: 60px;
  right: 10px;
  width: 340px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  background: rgba(18, 25, 38, 0.95);
  border: 1px solid rgba(0, 180, 255, 0.3);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  color: #e0e0e0;
  z-index: 300;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    background: rgba(0, 180, 255, 0.12);
    border-bottom: 1px solid rgba(0, 180, 255, 0.2);

    h3 { margin: 0; font-size: 16px; color: #fff; }
    .close-btn { cursor: pointer; color: #999; &:hover { color: #fff; } }
  }

  .detail-body {
    padding: 14px 16px;
  }

  .detail-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 10px;
    font-size: 13px;
    line-height: 1.5;
  }

  .detail-label {
    width: 50px;
    flex-shrink: 0;
    color: #8899aa;
    font-weight: 500;
  }

  .detail-section-title {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: #ccc;
    margin-bottom: 10px;
  }

  .no-files {
    font-size: 12px;
    color: #666;
    text-align: center;
    padding: 8px 0;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 0;
    border-bottom: 1px solid rgba(255,255,255,0.06);
    font-size: 12px;

    .file-name { flex: 1; color: #ccc; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .file-size { color: #888; }
    .file-action { color: #409eff; text-decoration: none; cursor: pointer; &:hover { color: #66b1ff; } }
  }

  .detail-actions {
    margin-top: 14px;
    text-align: center;
  }
}

/* 片区详情弹窗 (图三效果) */
.district-detail-panel {
  position: absolute;
  top: 60px;
  right: 10px;
  width: 380px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  background: rgba(10, 18, 32, 0.96);
  border: 1px solid rgba(0, 180, 255, 0.25);
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6);
  color: #e0e0e0;
  z-index: 350;

  .district-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 18px;
    border-bottom: 1px solid rgba(0, 180, 255, 0.15);

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 700;
    }
    .close-btn {
      cursor: pointer;
      color: #666;
      font-size: 18px;
      &:hover { color: #fff; }
    }
  }

  .district-detail-body {
    padding: 0;
  }

  .district-map-preview {
    padding: 16px 18px;
    border-bottom: 1px solid rgba(255,255,255,0.06);
    
    .map-placeholder {
      width: 100%;
      height: 160px;
      border-radius: 8px;
      border: 1px solid rgba(0, 180, 255, 0.2);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      .map-label {
        font-size: 20px;
        font-weight: 700;
        color: rgba(255,255,255,0.9);
        text-shadow: 0 2px 8px rgba(0,0,0,0.5);
      }
      .map-subtitle {
        font-size: 12px;
        color: rgba(255,255,255,0.4);
      }
    }
  }

  .district-info-section {
    padding: 14px 18px;
    border-bottom: 1px solid rgba(255,255,255,0.06);
    
    &:last-child { border-bottom: none; }
    
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #00b4d8;
      margin: 0 0 10px 0;
      position: relative;
      padding-left: 10px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 2px;
        width: 3px;
        height: 14px;
        background: #00b4d8;
        border-radius: 2px;
      }
    }
    
    .district-desc {
      margin: 0;
      font-size: 13px;
      line-height: 1.8;
      color: #b0bec5;
    }
  }

  .info-grid {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .info-item {
      display: flex;
      align-items: center;
      padding: 10px 14px;
      background: rgba(255,255,255,0.03);
      border: 1px solid rgba(255,255,255,0.06);
      border-radius: 6px;
      font-size: 13px;
      
      .info-label {
        width: 80px;
        flex-shrink: 0;
        color: #78909c;
      }
      .info-value {
        color: #e0e0e0;
        
        &.status-active {
          color: #69f0ae;
          font-weight: 600;
        }
      }
    }
  }
}

/* 片区管理员操作 */
.district-detail-panel .district-admin-actions {
  padding: 14px 18px;
  text-align: center;
  border-top: 1px solid rgba(255,255,255,0.06);
}

/* 8. Tools Panel */
.tools-panel {
  .tool-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    margin-bottom: 15px;
  }
  
  .tool-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 8px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    
    .el-icon {
      font-size: 24px;
      margin-bottom: 6px;
      color: #aaa;
    }
    
    span {
      font-size: 12px;
      color: #ccc;
    }
    
    &:hover {
      background: rgba(64, 158, 255, 0.15);
      border-color: rgba(64, 158, 255, 0.3);
      
      .el-icon { color: #409eff; }
    }
    
    &.active {
      background: rgba(64, 158, 255, 0.2);
      border-color: #409eff;
      
      .el-icon { color: #409eff; }
      span { color: #409eff; }
    }
  }
  
  .tool-subpanel {
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding-top: 15px;
    
    .sub-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 600;
      color: #fff;
    }
    
    .btn-group {
      display: flex;
      gap: 8px;
      margin-bottom: 10px;
    }
    
    .measure-result {
      background: rgba(0, 180, 255, 0.15);
      border: 1px solid rgba(0, 180, 255, 0.3);
      border-radius: 4px;
      padding: 10px;
      margin-top: 10px;
      
      .result-label {
        font-size: 12px;
        color: #aaa;
        margin-bottom: 4px;
      }
      
      .result-value {
        font-size: 18px;
        font-weight: 600;
        color: #00ffff;
      }
    }
    
    .tool-tip {
      font-size: 12px;
      color: #999;
      padding: 8px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 4px;
      margin-top: 10px;
    }
  }
  
  .bookmark-list, .marker-list {
    max-height: 200px;
    overflow-y: auto;
  }
  
  .bookmark-item, .marker-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: rgba(64, 158, 255, 0.15);
    }
    
    .bm-info, .mk-info {
      flex: 1;
      
      .bm-name, .mk-name {
        font-size: 14px;
        color: #fff;
        margin-bottom: 2px;
      }
      
      .bm-coords, .mk-desc {
        font-size: 11px;
        color: #888;
      }
    }
    
    .bm-delete, .mk-delete {
      color: #999;
      cursor: pointer;
      padding: 4px;
      
      &:hover { color: #f56c6c; }
    }
  }
  
  :deep(.el-form) {
    .el-form-item {
      margin-bottom: 12px;
    }
    
    .el-form-item__label {
      color: #ccc;
    }
    
    .el-input-number {
      width: 100%;
    }
  }
  
  /* Drawing list styles */
  .drawing-list {
    max-height: 200px;
    overflow-y: auto;
    margin-top: 10px;
  }
  
  .drawing-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 6px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: rgba(64, 158, 255, 0.15);
      border-color: rgba(64, 158, 255, 0.3);
    }
    
    .d-info {
      flex: 1;
      min-width: 0;
      
      .d-name {
        font-size: 14px;
        font-weight: 500;
        color: #fff;
        margin-bottom: 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .d-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-tag {
          font-size: 11px;
        }
        
        span {
          font-size: 11px;
          color: #888;
          background: rgba(255, 255, 255, 0.1);
          padding: 2px 6px;
          border-radius: 3px;
        }
      }
    }
    
    .d-delete {
      color: #999;
      cursor: pointer;
      padding: 6px;
      margin-left: 8px;
      border-radius: 4px;
      transition: all 0.2s;
      
      &:hover {
        color: #f56c6c;
        background: rgba(245, 108, 108, 0.1);
      }
    }
  }
}

/* 9. Spatial Analysis Styles */
.btn-group.vertical {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  .el-button {
    margin: 0;
    justify-content: flex-start;
  }
}

.spatial-params {
  margin-top: 15px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  
  .unit {
    margin-left: 8px;
    color: #999;
    font-size: 12px;
  }
}

.spatial-result {
  margin-top: 15px;
  background: rgba(0, 180, 255, 0.1);
  border: 1px solid rgba(0, 180, 255, 0.3);
  border-radius: 4px;
  overflow: hidden;
  
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: rgba(0, 180, 255, 0.15);
    font-weight: 600;
    font-size: 13px;
    
    .close-btn {
      cursor: pointer;
      &:hover { color: #f56c6c; }
    }
  }
  
  .result-body {
    padding: 12px;
    font-size: 13px;
    
    p {
      margin: 0 0 8px;
      
      strong { color: #aaa; }
    }
    
    .prop-row {
      display: flex;
      margin-bottom: 4px;
      
      .prop-key { color: #888; width: 100px; }
      .prop-val { color: #fff; flex: 1; }
    }
  }
  
  .feature-list {
    max-height: 120px;
    overflow-y: auto;
    margin-top: 8px;
    
    .feature-item {
      padding: 4px 8px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 3px;
      margin-bottom: 4px;
      font-size: 12px;
      color: #ccc;
    }
  }
  
  .stat-breakdown {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    
    .stat-item {
      display: flex;
      justify-content: space-between;
      padding: 4px 0;
      font-size: 12px;
      color: #ccc;
    }
  }
}

/* 10. Dashboard Overlay Panels */
.dashboard-overlay {
  position: absolute;
  top: 70px;
  left: 0;
  right: 0;
  bottom: 40px;
  pointer-events: none;
  z-index: 50;
  display: flex;
  justify-content: space-between;
  padding: 15px;
}

.chart-panel {
  pointer-events: auto;
  width: 380px;
  background: rgba(10, 20, 40, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 180, 255, 0.3);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  
  &.left-panel {
    margin-right: auto;
  }
  
  &.right-panel {
    margin-left: auto;
  }
  
  .panel-title {
    padding: 12px 16px;
    font-size: 16px;
    font-weight: 600;
    color: #00e5ff;
    border-bottom: 1px solid rgba(0, 180, 255, 0.2);
    background: rgba(0, 180, 255, 0.1);
    
    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 16px;
      background: linear-gradient(180deg, #00e5ff, #0096ff);
      margin-right: 10px;
      vertical-align: middle;
      border-radius: 2px;
    }
  }
  
  .chart-container {
    flex: 1;
    min-height: 280px;
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    font-size: 14px;

    &::after {
      content: '图表加载中...';
    }
  }
}

/* 附件列表优化样式 */
.file-item-new {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.08);

  &:hover {
    background: rgba(64, 158, 255, 0.12);
    border-color: rgba(64, 158, 255, 0.4);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }

  .file-info-main {
    display: flex;
    align-items: center;
    gap: 14px;
    flex: 1;
    overflow: hidden;

    .file-type-icon {
      font-size: 26px;
      color: #409eff;
      flex-shrink: 0;
      filter: drop-shadow(0 0 4px rgba(64, 158, 255, 0.3));
    }

    .file-text {
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .file-name-new {
        color: #efefef;
        font-size: 14px;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .file-size-new {
        color: #909399;
        font-size: 12px;
        margin-top: 2px;
      }
    }
  }

  .file-actions-new {
    display: flex;
    gap: 10px;
    margin-left: 12px;
    flex-shrink: 0;

    .icon-link-btn {
      text-decoration: none;
      display: flex;
    }
    
    :deep(.el-button.is-circle) {
      padding: 7px;
    }
  }
}

.no-files {
  text-align: center;
  color: #909399;
  padding: 30px 0;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  margin: 10px 0;
}

.district-files-section {
  .section-title {
    font-size: 15px;
    color: #409eff;
    margin-bottom: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      width: 3px;
      height: 14px;
      background: #409eff;
      margin-right: 8px;
      border-radius: 2px;
    }
  }
}

/* ========== 土地储备模块样式 ========== */
.land-summary-text {
  font-size: 13px;
  color: #b0c4de;
  line-height: 2;
  padding: 12px 14px;
  background: rgba(0, 180, 255, 0.06);
  border: 1px solid rgba(0, 180, 255, 0.15);
  border-radius: 6px;
  margin-bottom: 16px;
}

.land-section-label {
  font-size: 14px;
  color: #00e5ff;
  margin: 16px 0 10px;
  font-weight: 600;
}

.land-stat-cards {
  display: flex;
  gap: 12px;

  .land-stat-card {
    flex: 1;
    background: rgba(0, 180, 255, 0.08);
    border: 1px solid rgba(0, 180, 255, 0.2);
    border-radius: 8px;
    padding: 14px 12px;
    text-align: center;
    transition: all 0.3s;

    &:hover {
      background: rgba(0, 180, 255, 0.15);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 180, 255, 0.2);
    }

    .land-stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #00e5ff;
      text-shadow: 0 0 8px rgba(0, 229, 255, 0.4);
    }

    .land-stat-label {
      font-size: 12px;
      color: #8899aa;
      margin-top: 4px;
    }
  }
}

.land-table-wrapper {
  overflow-x: auto;
}

.land-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  th {
    background: rgba(0, 180, 255, 0.12);
    color: #8ecae6;
    padding: 8px 10px;
    text-align: center;
    border-bottom: 1px solid rgba(0, 180, 255, 0.25);
    font-weight: 600;
  }

  td {
    padding: 8px 10px;
    text-align: center;
    color: #ccc;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }

  tr:hover td {
    background: rgba(0, 180, 255, 0.08);
  }

  .land-table-total {
    td {
      background: rgba(0, 180, 255, 0.06);
      color: #00e5ff;
      border-top: 1px solid rgba(0, 180, 255, 0.25);
    }
  }
}

.land-detail-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .land-detail-label {
    width: 80px;
    flex-shrink: 0;
    color: #8899aa;
    font-size: 13px;
  }

  .land-detail-val {
    flex: 1;
    color: #efefef;
    font-size: 14px;
  }
}

.eng-filter-bar {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 16px;
  z-index: 100;
  pointer-events: auto; /* 允许点击，修复被外层覆盖的问题 */

  .eng-filter-btn {
    padding: 8px 28px;
    border: 1px solid rgba(0, 229, 255, 0.5);
    background: rgba(0, 20, 40, 0.8);
    color: #00e5ff;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
    clip-path: polygon(8px 0, 100% 0, calc(100% - 8px) 100%, 0 100%);
    pointer-events: auto; /* 确保自身可点击 */

    &:hover {
      background: rgba(0, 229, 255, 0.15);
    }

    &.active {
      background: rgba(0, 229, 255, 0.3);
      color: #fff;
      border-color: #00e5ff;
      box-shadow: 0 0 10px rgba(0, 229, 255, 0.4);
    }
  }
}
</style>

<style lang="scss">
/* 科技风深色弹窗覆盖样式 (全局生效，避免 scoped 无法作用于 body 下的弹窗) */
.project-edit-dialog {
  background: rgba(8, 26, 46, 0.95) !important;
  border: 1px solid rgba(0, 229, 255, 0.3) !important;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.2) !important;
  backdrop-filter: blur(10px);

  .el-dialog__header {
    border-bottom: 1px solid rgba(0, 229, 255, 0.2);
    margin-right: 0;
    padding-bottom: 15px;

    .el-dialog__title {
      color: #00e5ff;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(0, 229, 255, 0.5);
    }
    
    .el-dialog__headerbtn .el-dialog__close {
      color: #00e5ff;
    }
  }

  .el-dialog__body {
    padding: 20px 30px;
    color: #e0e0e0;
  }

  .el-dialog__footer {
    border-top: 1px solid rgba(0, 229, 255, 0.2);
    padding-top: 15px;
  }

  /* 表单控件暗色化 */
  .el-form-item__label {
    color: #8899aa;
  }

  /* 适配各种版本 Element Plus 的 input, select */
  .el-input__inner,
  .el-textarea__inner,
  .el-input-number__decrease,
  .el-input-number__increase {
    background-color: transparent !important;
    color: #00e5ff !important;
  }

  .el-input__wrapper,
  .el-select__wrapper,
  .el-textarea__inner,
  .el-input-number__decrease,
  .el-input-number__increase {
    background-color: rgba(0, 0, 0, 0.3) !important;
    border: 1px solid rgba(0, 229, 255, 0.2) !important;
    box-shadow: none !important;
    color: #00e5ff !important;

    &:hover, &.is-focus, &.is-active {
      border-color: rgba(0, 229, 255, 0.6) !important;
      box-shadow: 0 0 5px rgba(0, 229, 255, 0.3) !important;
    }
  }

  .el-input__inner::placeholder,
  .el-textarea__inner::placeholder {
    color: rgba(136, 153, 170, 0.5);
  }

  .el-input-number__decrease,
  .el-input-number__increase {
    border-radius: 0 !important;
    border-left: 1px solid rgba(0, 229, 255, 0.2) !important;
    border-right: 1px solid rgba(0, 229, 255, 0.2) !important;
    color: #00e5ff !important;
    &:hover { color: #fff !important; }
  }

  .el-button {
    background-color: transparent;
    border: 1px solid rgba(0, 229, 255, 0.5);
    color: #00e5ff;

    &:hover {
      background-color: rgba(0, 229, 255, 0.1);
      border-color: #00e5ff;
    }

    &.el-button--primary {
      background-color: rgba(0, 229, 255, 0.15);
      border-color: #00e5ff;
      
      &:hover {
        background-color: rgba(0, 229, 255, 0.3);
      }
    }
  }
}

/* 下拉菜单/日期选择器的暗黑浮层 */
.dark-popper {
  background: rgba(8, 26, 46, 0.95) !important;
  border: 1px solid rgba(0, 229, 255, 0.3) !important;
  backdrop-filter: blur(10px);
  
  .el-select-dropdown__item {
    color: #e0e0e0 !important;
    background-color: transparent !important;
    
    &.hover, &:hover {
      background-color: rgba(0, 229, 255, 0.15) !important;
      color: #00e5ff !important;
    }
    &.selected {
      color: #00e5ff !important;
      background-color: rgba(0, 229, 255, 0.3) !important;
      font-weight: bold;
    }
  }
  
  .el-picker-panel, .el-date-picker {
    background: transparent !important;
    color: #e0e0e0;
    border: none !important;

    .el-date-table td.available:hover { color: #00e5ff; }
    .el-date-table td.current:not(.disabled) .el-date-table-cell__text {
      background-color: #00e5ff;
      color: #000;
    }
    .el-date-picker__header-label { color: #00e5ff; }
    .el-picker-panel__icon-btn { color: #8899aa; }
  }

  .el-popper__arrow::before {
    background: rgba(8, 26, 46, 0.95) !important;
    border: 1px solid rgba(0, 229, 255, 0.3) !important;
  }
}


/* ===== 危险作业模块样式 ===== */
.danger-stat-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 12px 0;
}

.danger-stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  border-left: 3px solid rgba(0, 212, 255, 0.5);
}

.danger-stat-label { color: rgba(255, 255, 255, 0.75); font-size: 13px; }
.danger-stat-num { font-size: 22px; font-weight: bold; font-family: 'DIN Alternate', monospace; }
.danger-red { color: #F56C6C; }
.danger-yellow { color: #E6A23C; }
.danger-green { color: #67C23A; }
.danger-blue { color: #409EFF; }

.danger-record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.danger-record-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 6px;
  border-left: 2px solid rgba(0, 212, 255, 0.3);
  flex-wrap: wrap;
}

.record-time { font-size: 11px; color: rgba(255, 255, 255, 0.45); white-space: nowrap; }
.record-name { flex: 1; font-size: 12px; color: rgba(255, 255, 255, 0.85); min-width: 80px; }

/* ===== 监控详情弹窗 ===== */
.monitor-popup-body {
  display: flex;
  gap: 20px;
  height: 480px;
}

.monitor-video-area {
  flex: 0 0 55%;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.webcam-controls {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.6);
  padding: 6px 14px;
  border-radius: 20px;
}

.monitor-info-area {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.monitor-info-card {
  background: rgba(0, 21, 41, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  padding: 14px 16px;
}

.monitor-info-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.monitor-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
  font-size: 13px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  
  span { color: rgba(255, 255, 255, 0.55); white-space: nowrap; }
  strong { color: #e0e0e0; }
}

.monitor-risk-alert {
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 8px;
}

.risk-critical { background: rgba(245, 108, 108, 0.2); border: 1px solid rgba(245, 108, 108, 0.5); color: #F56C6C; }
.risk-major    { background: rgba(230, 162, 60, 0.2); border: 1px solid rgba(230, 162, 60, 0.5); color: #E6A23C; }
.risk-normal   { background: rgba(103, 194, 58, 0.2); border: 1px solid rgba(103, 194, 58, 0.5); color: #67C23A; }

.safety-measure-item {
  padding: 5px 4px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
</style>

<style lang="scss">
/* ===== 危险作业模块深色弹窗主题 (全局应用，以覆盖 append-to-body) ===== */
.dark-dialog {
  &.el-dialog,
  .el-dialog {
    background: rgba(5, 16, 35, 0.97) !important;
    border: 1px solid rgba(0, 212, 255, 0.3) !important;
    border-radius: 10px !important;
    box-shadow: 0 0 40px rgba(0, 212, 255, 0.15) !important;
    color: #e0e0e0 !important;
  }

  .el-dialog__header {
    background: rgba(0, 21, 41, 0.95) !important;
    border-bottom: 1px solid rgba(0, 212, 255, 0.2) !important;
    padding: 16px 20px !important;
    border-radius: 10px 10px 0 0 !important;
  }

  .el-dialog__title {
    color: #00d4ff !important;
    font-size: 16px !important;
    font-weight: bold !important;
    letter-spacing: 0.5px !important;
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: rgba(255, 255, 255, 0.6) !important;
    font-size: 18px !important;
    &:hover { color: #F56C6C !important; }
  }

  .el-dialog__body {
    background: rgba(5, 16, 35, 0.97) !important;
    color: #e0e0e0 !important;
    padding: 20px !important;
    border-radius: 0 0 10px 10px !important;
  }

  .el-dialog__footer {
    background: rgba(0, 21, 41, 0.85) !important;
    border-top: 1px solid rgba(0, 212, 255, 0.15) !important;
    padding: 12px 20px !important;
    border-radius: 0 0 10px 10px !important;
  }

  /* 表单控件深色化 */
  .el-form-item__label { color: rgba(255, 255, 255, 0.75) !important; }

  .el-input__wrapper,
  .el-textarea__inner {
    background: rgba(255, 255, 255, 0.06) !important;
    border-color: rgba(0, 212, 255, 0.3) !important;
    box-shadow: none !important;
    color: #e0e0e0 !important;

    &.is-focus { border-color: #00d4ff !important; }
  }

  .el-input__inner,
  .el-textarea__inner {
    color: #e0e0e0 !important;
    background: transparent !important;
    &::placeholder { color: rgba(255, 255, 255, 0.35) !important; }
  }

  .el-select .el-input__wrapper {
    background: rgba(255, 255, 255, 0.06) !important;
    border-color: rgba(0, 212, 255, 0.3) !important;
  }

  .el-date-editor .el-input__wrapper {
    background: rgba(255, 255, 255, 0.06) !important;
    border-color: rgba(0, 212, 255, 0.3) !important;
  }

  .el-checkbox__label { color: #e0e0e0 !important; }
  .el-checkbox-group { color: #e0e0e0 !important; }

  /* 表格深色 */
  .el-table {
    background: transparent !important;
    --el-table-bg-color: rgba(0, 15, 30, 0.7);
    --el-table-tr-bg-color: rgba(0, 15, 30, 0.5);
    --el-table-row-hover-bg-color: rgba(0, 212, 255, 0.1);
    --el-table-border-color: rgba(0, 212, 255, 0.15);
    --el-table-text-color: #e0e0e0;
    --el-table-header-text-color: #00d4ff;
    --el-table-header-bg-color: rgba(0, 21, 41, 0.9);
    color: #e0e0e0 !important;
  }
}
</style>
