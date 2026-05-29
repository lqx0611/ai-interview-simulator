import axios from 'axios';
import Cookies from 'js-cookie';

/** Cookie中存储Token的key */
const TOKEN_KEY = 'auth_token';

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
 * 请求拦截器
 * 自动从Cookie中读取Token，添加到Authorization Header
 */
request.interceptors.request.use((config) => {
  const token = Cookies.get(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * 响应拦截器
 * - 自动解包后端统一返回结构 Result<T>
 * - code !== 200 时统一走 reject 分支
 * - 401 时清除Token并跳转登录页
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
    // 401 未认证：清除Token和用户信息，跳转登录页
    if (error.response?.status === 401) {
      logout();
      // 避免在登录/注册页重复跳转
      if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
        window.location.href = '/login';
      }
    }
    console.error('Network error:', error.message);
    return Promise.reject(error);
  }
);

/** 保存Token到Cookie（有效期7天，与后端JWT一致） */
export function saveToken(token: string) {
  Cookies.set(TOKEN_KEY, token, { expires: 7 });
}

/** 清除Cookie中的Token */
export function removeToken() {
  Cookies.remove(TOKEN_KEY);
}

/** 从Cookie获取Token */
export function getToken(): string | undefined {
  return Cookies.get(TOKEN_KEY);
}

/** 判断是否已登录（仅检查Cookie中是否有Token） */
export function isLoggedIn(): boolean {
  return !!Cookies.get(TOKEN_KEY);
}

// ─────────────────────────── 用户信息本地存储 ───────────────────────────

const USER_KEY = 'auth_user';

/** 用户信息（存储于localStorage，用于页面展示用户名/昵称，不从Cookie读敏感信息） */
export interface StoredUser {
  id: number;
  username: string;
  nickname: string;
}

/** 保存用户信息到localStorage */
export function saveUserInfo(user: StoredUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

/** 从localStorage读取用户信息 */
export function getUserInfo(): StoredUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredUser;
  } catch {
    return null;
  }
}

/** 清除localStorage中的用户信息 */
export function removeUserInfo() {
  localStorage.removeItem(USER_KEY);
}

/** 完整登出：清除Token + 清除用户信息 */
export function logout() {
  removeToken();
  removeUserInfo();
}

export default request;
