import request from './request';

// ─────────────────────────── 类型定义 ───────────────────────────

/** 注册请求参数 */
export interface RegisterParams {
  /** 用户名，3-20字符 */
  username: string;
  /** 密码，6-20字符 */
  password: string;
  /** 昵称（可选） */
  nickname?: string;
}

/** 登录请求参数 */
export interface LoginParams {
  username: string;
  password: string;
}

/** 用户基本信息（不含密码） */
export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
}

/** 登录响应 */
export interface LoginResult {
  token: string;
  user: UserInfo;
}

// ─────────────────────────── API 方法 ───────────────────────────

/**
 * 用户注册
 * POST /api/auth/register
 */
export async function register(params: RegisterParams): Promise<UserInfo> {
  const res = await request.post('/auth/register', params);
  return res.data;
}

/**
 * 用户登录
 * POST /api/auth/login
 */
export async function login(params: LoginParams): Promise<LoginResult> {
  const res = await request.post('/auth/login', params);
  return res.data;
}
