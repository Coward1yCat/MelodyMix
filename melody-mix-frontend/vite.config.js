// vite.config.js

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 1. ⬅️ 导入 Node.js 的 'path' 模块
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  // 2. ⬅️ 在这里添加 resolve.alias 配置
  resolve: {
    alias: {
      // 这行代码告诉 Vite，'@' 这个别名实际上指向的是项目根目录下的 'src' 文件夹
      '@': path.resolve(__dirname, './src'),
    }
  },

  // server 部分保持不变
  server: {
    port: 5173,
    proxy: {
      '/api': { 
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    }
  }
})
