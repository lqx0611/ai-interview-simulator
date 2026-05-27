import axios from 'axios';

/**
 * Axios 实例 — 封装公共请求配置
 * - baseURL: /api，由 Vite 代理转发到后端 8080 端口
 * - timeout: 30秒超时
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

/**
 * 响应拦截器
 * - 自动解包后端统一返回结构 Result<T>
 * - code !== 200 时作为错误处理，统一走 reject 分支
 */
request.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.code !== 200) {
      console.error(`API Error [${data.code}]: ${data.message}`);
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return data;
  },
  (error) => {
    console.error('Network error:', error.message);
    return Promise.reject(error);
  }
);

export default request;
