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

export async function startInterview(params: StartInterviewParams): Promise<StartInterviewResult> {
  const res = await request.post('/interview/start', params);
  return res.data;
}

export async function endInterview(id: number): Promise<EndInterviewResult> {
  const res = await request.post(`/interview/${id}/end`);
  return res.data;
}
