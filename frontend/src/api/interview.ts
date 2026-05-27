import request from './request';

export interface StartInterviewParams {
  direction: string;
  difficulty: string;
  interviewType: string;
}

export interface StartInterviewResult {
  interviewId: number;
  openingMessage: string;
}

export interface EndInterviewResult {
  reportId: number;
  overallScore: number;
  summary: string;
  topicScores: { topic: string; score: number; comment: string; isWeak: boolean }[];
  improvement: string;
  durationSeconds: number;
  questionCount: number;
}

export interface HistoryItem {
  id: number;
  direction: string;
  difficulty: string;
  interviewType: string;
  totalScore: number;
  questionCount: number;
  durationSeconds: number;
  createTime: string;
}

export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

export interface InterviewDetail {
  id: number;
  direction: string;
  difficulty: string;
  interviewType: string;
  totalScore: number;
  questionCount: number;
  durationSeconds: number;
  createTime: string;
  messages: {
    role: string;
    content: string;
    topic: string;
    score: number | null;
    createTime: string;
  }[];
  report: {
    reportId: number;
    overallScore: number;
    summary: string;
    improvement: string;
    topicScores: {
      topic: string;
      score: number;
      comment: string;
      isWeak: boolean;
    }[];
  } | null;
}

export async function startInterview(params: StartInterviewParams): Promise<StartInterviewResult> {
  const res = await request.post('/interview/start', params);
  return res.data;
}

export async function endInterview(id: number): Promise<EndInterviewResult> {
  const res = await request.post(`/interview/${id}/end`);
  return res.data;
}

export async function getHistory(page = 1, size = 10): Promise<PageResponse<HistoryItem>> {
  const res = await request.get('/interview/history', { params: { page, size } });
  return res.data;
}

export async function getInterviewDetail(id: number): Promise<InterviewDetail> {
  const res = await request.get(`/interview/${id}/detail`);
  return res.data;
}

export interface DashboardStats {
  totalInterviews: number;
  totalDurationSeconds: number;
  topicStats: {
    topic: string;
    avgScore: number;
    maxScore: number;
    minScore: number;
    practiceCount: number;
    level: string;
    lastPracticeTime: string | null;
  }[];
  weakTopics: string[];
}

export async function getDashboardStats(): Promise<DashboardStats> {
  const res = await request.get('/dashboard/stats');
  return res.data;
}
