import request from './request';

// ─────────────────────────── 类型定义 ───────────────────────────

/** 开始面试请求参数 */
export interface StartInterviewParams {
  /** 面试方向：java_backend / ai_dev / fullstack */
  direction: string;
  /** 难度等级：junior / mid / senior */
  difficulty: string;
  /** 面试类型：knowledge / project / comprehensive */
  interviewType: string;
}

/** 开始面试响应 */
export interface StartInterviewResult {
  /** 新创建的面试记录ID */
  interviewId: number;
  /** AI面试官的开场消息（含第一个问题） */
  openingMessage: string;
}

/** 结束面试响应 */
export interface EndInterviewResult {
  /** 生成的报告ID */
  reportId: number;
  /** 整体评分（1-10） */
  overallScore: number;
  /** AI生成的面试总结 */
  summary: string;
  /** 各知识点评分明细 */
  topicScores: { topic: string; score: number; comment: string; isWeak: boolean }[];
  /** AI生成的改进建议 */
  improvement: string;
  /** 面试总时长（秒） */
  durationSeconds: number;
  /** 总提问数 */
  questionCount: number;
}

/** 历史列表项 */
export interface HistoryItem {
  id: number;
  /** 面试方向 */
  direction: string;
  /** 难度等级 */
  difficulty: string;
  /** 面试类型 */
  interviewType: string;
  /** 总评分 */
  totalScore: number;
  /** 提问数 */
  questionCount: number;
  /** 面试时长（秒） */
  durationSeconds: number;
  /** 面试时间（ISO字符串） */
  createTime: string;
}

/** 通用分页响应 */
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

/** 面试详情（含完整对话和报告） */
export interface InterviewDetail {
  id: number;
  direction: string;
  difficulty: string;
  interviewType: string;
  totalScore: number;
  questionCount: number;
  durationSeconds: number;
  createTime: string;
  /** 完整对话消息列表 */
  messages: {
    /** 角色：interviewer(面试官) / candidate(候选人) */
    role: string;
    content: string;
    /** 当前考察知识点 */
    topic: string;
    /** AI评分（candidate消息） */
    score: number | null;
    createTime: string;
  }[];
  /** 面试报告（未结束时为null） */
  report: {
    reportId: number;
    overallScore: number;
    summary: string;
    improvement: string;
    /** 知识点评分列表 */
    topicScores: {
      topic: string;
      score: number;
      comment: string;
      /** 是否薄弱项 */
      isWeak: boolean;
    }[];
  } | null;
}

/** 看板统计数据 */
export interface DashboardStats {
  /** 总面试次数 */
  totalInterviews: number;
  /** 总练习时长（秒） */
  totalDurationSeconds: number;
  /** 各知识点统计 */
  topicStats: {
    topic: string;
    /** 平均分（1-10） */
    avgScore: number;
    /** 最高分 */
    maxScore: number;
    /** 最低分 */
    minScore: number;
    /** 练习次数 */
    practiceCount: number;
    /** 掌握度等级：proficient / skilled / familiar / weak */
    level: string;
    /** 最近练习时间（ISO字符串） */
    lastPracticeTime: string | null;
  }[];
  /** 薄弱知识点名称列表 */
  weakTopics: string[];
}

// ─────────────────────────── API 方法 ───────────────────────────

/**
 * 开始面试
 * POST /api/interview/start
 */
export async function startInterview(params: StartInterviewParams): Promise<StartInterviewResult> {
  const res = await request.post('/interview/start', params);
  return res.data;
}

/**
 * 结束面试
 * POST /api/interview/{id}/end
 */
export async function endInterview(id: number): Promise<EndInterviewResult> {
  const res = await request.post(`/interview/${id}/end`);
  return res.data;
}

/**
 * 分页获取历史面试列表
 * GET /api/interview/history?page=&size=
 */
export async function getHistory(page = 1, size = 10): Promise<PageResponse<HistoryItem>> {
  const res = await request.get('/interview/history', { params: { page, size } });
  return res.data;
}

/**
 * 获取面试详情（完整对话 + 报告）
 * GET /api/interview/{id}/detail
 */
export async function getInterviewDetail(id: number): Promise<InterviewDetail> {
  const res = await request.get(`/interview/${id}/detail`);
  return res.data;
}

/**
 * 获取首页看板统计数据
 * GET /api/dashboard/stats
 */
export async function getDashboardStats(): Promise<DashboardStats> {
  const res = await request.get('/dashboard/stats');
  return res.data;
}
