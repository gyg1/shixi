/**
 * 应用配置
 */
export const config = {
  // API 基础URL
  apiBaseUrl: 'http://localhost:8080/api',

  // Cesium Ion Token
  cesiumToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI1MGQ0YmMzYy05YWFmLTRkMDctYmUxYy01NmEwZDhjNzk4M2QiLCJpZCI6MzUyNTAzLCJpYXQiOjE3Njg1MjkxODJ9.yzV1d4ngdCdLWfEUaPb2Ja_x9xuLYVGN3POxmbVVxbE',

  // 天地图 Token
  tiandituToken: 'abf1d23efb709a79bb93553d8410ba24',

  // 天地图服务URL
  tianditu: {
    // 矢量底图
    vec: 'https://t{s}.tianditu.gov.cn/vec_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=',
    // 矢量注记
    cva: 'https://t{s}.tianditu.gov.cn/cva_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=',
    // 影像底图
    img: 'https://t{s}.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=',
    // 影像注记
    cia: 'https://t{s}.tianditu.gov.cn/cia_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cia&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=',
    // 地形晕渲
    ter: 'https://t{s}.tianditu.gov.cn/ter_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=ter&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk='
  },

  // 地图默认中心点 (石家庄)
  defaultCenter: {
    longitude: 114.48,
    latitude: 38.03,
    height: 50000
  },

  // 地图图层配置
  layers: [
    { id: 'shijiazhuang', name: '石家庄行政区划', file: 'shijiazhuang.geojson', visible: true, type: 'boundary' }
  ],

  // 图层颜色配置
  layerColors: {
    boundary: { fill: 'rgba(100, 149, 237, 0.1)', stroke: 'rgba(100, 149, 237, 0.8)' }
  }
}

export default config
