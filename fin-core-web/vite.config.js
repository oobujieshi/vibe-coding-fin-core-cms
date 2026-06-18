import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
      proxy: {
      '/api/v1/accounts': { target: 'http://localhost:8083', changeOrigin: true },
      '/api/v1/transactions': { target: 'http://localhost:8083', changeOrigin: true },
      '/api/v1/reconciliations': { target: 'http://localhost:8083', changeOrigin: true },
      '/api/v1/reports': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/v1/receipts': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/v1/payments': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/v1/bills': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/v1/approvals': { target: 'http://localhost:8082', changeOrigin: true },
      '/api': { target: 'http://localhost:8081', changeOrigin: true },
    },
  },
})
