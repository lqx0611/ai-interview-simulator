import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Vite构建配置
 * - React插件：支持JSX编译和Fast Refresh
 * - 开发服务器端口：3000
 * - API代理：/api -> http://localhost:8080（后端Spring Boot服务）
 * - SSE支持：关闭代理层响应缓冲
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,  // 开发服务器端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端Spring Boot地址
        changeOrigin: true,  // 修改请求头Origin为target地址
        configure: (proxy) => {
          // SSE流式输出需要禁用代理层缓冲，否则数据会积压才推送
          proxy.on('proxyRes', (proxyRes) => {
            // 禁用HTTP缓存，确保每次请求都获取最新数据
            proxyRes.headers['cache-control'] = 'no-cache';
            // 禁用Nginx的代理缓冲（开发环境兼容）
            proxyRes.headers['x-accel-buffering'] = 'no';
          });
        },
      },
    },
  },
})
