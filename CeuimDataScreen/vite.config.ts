import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import cesium from 'vite-plugin-cesium'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueDevTools(),
        cesium()
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        },
    },
    server: {
        proxy: {
            // 后端 API 代理
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            // 天地图代理 - 解决 CORS 问题
            '/tianditu': {
                target: 'http://t0.tianditu.gov.cn',
                changeOrigin: true,
                secure: false,
                rewrite: (path) => path.replace(/^\/tianditu/, '')
            }
        }
    }
})
