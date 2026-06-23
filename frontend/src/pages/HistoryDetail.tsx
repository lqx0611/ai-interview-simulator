/**
 * 历史详情页
 * 展示单次面试的完整信息：基本信息、完整对话记录、面试报告（含知识点评分）
 */
import { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Card, Divider, Empty, Progress, Space, Spin, Tag, Typography } from 'antd';
import {
  ArrowLeftOutlined, BulbOutlined, ClockCircleOutlined,
  QuestionCircleOutlined, WarningOutlined, ReloadOutlined,
} from '@ant-design/icons';
import ChatBubble from '../components/ChatBubble';
import { getInterviewDetail, type InterviewDetail } from '../api/interview';

const { Title, Text, Paragraph } = Typography;

const DIR_LABELS: Record<string, string> = {
  java_backend: 'Java后端', ai_dev: 'AI开发', fullstack: '全栈开发',
};
const DIR_COLORS: Record<string, string> = {
  java_backend: 'blue', ai_dev: 'purple', fullstack: 'green',
};
const DIFF_LABELS: Record<string, string> = {
  junior: '初级', mid: '中级', senior: '高级',
};

const formatDuration = (s: number) => {
  const m = Math.floor(s / 60);
  const sec = s % 60;
  if (m > 0) return `${m}分${sec}秒`;
  return `${sec}秒`;
};

const formatDate = (dateStr: string) => {
  const d = new Date(dateStr);
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  });
};

/** 根据分数返回对应颜色：>=8 绿, >=6 蓝, >=4 黄, <4 红 */
const scoreColor = (score: number) => {
  if (score >= 8) return '#52c41a';
  if (score >= 6) return '#1677ff';
  if (score >= 4) return '#faad14';
  return '#ff4d4f';
};

const HistoryDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [data, setData] = useState<InterviewDetail | null>(null);
  const [loading, setLoading] = useState(true);

  // 评分动画：从0递增到目标分数
  const [displayScore, setDisplayScore] = useState(0);
  const animRef = useRef<ReturnType<typeof setInterval> | null>(null);

  /** 根据URL参数中的面试ID加载详情数据 */
  useEffect(() => {
    if (!id) return;
    getInterviewDetail(Number(id))
      .then(setData)
      .finally(() => setLoading(false));
  }, [id]);

  // 数据加载完成后启动评分动画
  useEffect(() => {
    if (!data?.report?.overallScore) return;
    const target = data.report.overallScore;
    let current = 0;
    const step = Math.max(0.05, target / 50);
    animRef.current = setInterval(() => {
      current += step;
      if (current >= target) {
        setDisplayScore(target);
        clearInterval(animRef.current!);
      } else {
        setDisplayScore(current);
      }
    }, 20);
    return () => { if (animRef.current) clearInterval(animRef.current); };
  }, [data?.report?.overallScore]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!data) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Empty description="面试记录不存在" />
      </div>
    );
  }

  const report = data.report;
  const weakTopics = report?.topicScores.filter(t => t.isWeak) ?? [];
  const strongTopics = report?.topicScores.filter(t => !t.isWeak) ?? [];

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '32px 20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <ArrowLeftOutlined
          onClick={() => navigate('/history')}
          style={{ fontSize: 18, cursor: 'pointer', color: '#1677ff' }}
        />
        <Title level={3} style={{ margin: 0 }}>面试详情</Title>
      </div>

      {/* Basic Info */}
      <Card style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
          <Space>
            <Tag color={DIR_COLORS[data.direction] || 'default'}>
              {DIR_LABELS[data.direction] || data.direction}
            </Tag>
            <Tag>{DIFF_LABELS[data.difficulty] || data.difficulty}</Tag>
          </Space>
          <Space size={24}>
            <span><ClockCircleOutlined style={{ marginRight: 4 }} />{formatDuration(data.durationSeconds)}</span>
            <span><QuestionCircleOutlined style={{ marginRight: 4 }} />{data.questionCount} 题</span>
            <Text type="secondary" style={{ fontSize: 12 }}>{formatDate(data.createTime)}</Text>
          </Space>
        </div>
      </Card>

      {/* Conversation */}
      <Card title="对话记录" style={{ marginBottom: 24 }}>
        <div style={{ maxHeight: 500, overflowY: 'auto' }}>
          {data.messages.map((msg, i) => (
            <ChatBubble
              key={i}
              role={msg.role as 'interviewer' | 'candidate'}
              content={msg.content}
            />
          ))}
        </div>
      </Card>

      {/* Report */}
      {report ? (
        <>
          <Card style={{ marginBottom: 24, textAlign: 'center' }}>
            <Title level={5} style={{ marginBottom: 16 }}>面试评分</Title>
            <Progress
              type="circle"
              percent={Math.round(displayScore * 10)}
              size={140}
              strokeColor={scoreColor(report.overallScore)}
              format={() => (
                <div>
                  <div style={{ fontSize: 36, fontWeight: 700, color: scoreColor(report.overallScore), lineHeight: 1 }}>
                    {displayScore.toFixed(1)}
                  </div>
                  <div style={{ fontSize: 12, color: '#999' }}>/ 10</div>
                </div>
              )}
            />
            <Paragraph style={{ marginTop: 16, whiteSpace: 'pre-wrap', color: '#555', textAlign: 'left' }}>
              {report.summary}
            </Paragraph>
          </Card>

          {/* Topic Scores */}
          {report.topicScores.length > 0 && (
            <Card title={
              <span>
                知识点评分
                {weakTopics.length > 0 && <Tag color="error" style={{ marginLeft: 8 }}>{weakTopics.length} 个薄弱点</Tag>}
              </span>
            } style={{ marginBottom: 24 }}>
              {strongTopics.length > 0 && (
                <>
                  <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>已掌握</Text>
                  {strongTopics.map(t => (
                    <div key={t.topic} style={{ display: 'flex', alignItems: 'center', marginBottom: 16, gap: 12 }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                          <Text strong>{t.topic}</Text>
                          <Text style={{ color: scoreColor(t.score) }}>{t.score}/10</Text>
                        </div>
                        <Progress percent={t.score * 10} strokeColor={scoreColor(t.score)} showInfo={false} size="small" />
                      </div>
                    </div>
                  ))}
                </>
              )}

              {weakTopics.length > 0 && strongTopics.length > 0 && <Divider style={{ margin: '8px 0 16px' }} />}

              {weakTopics.length > 0 && (
                <>
                  <Text type="danger" style={{ display: 'block', marginBottom: 12 }}><WarningOutlined /> 薄弱环节</Text>
                  {weakTopics.map(t => (
                    <div key={t.topic} style={{
                      background: '#fff2f0', border: '1px solid #ffccc7', borderRadius: 8,
                      padding: '12px 16px', marginBottom: 12,
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                        <Space>
                          <Text strong style={{ color: '#ff4d4f' }}>{t.topic}</Text>
                          <Tag color="error">薄弱</Tag>
                        </Space>
                        <Text style={{ color: '#ff4d4f', fontWeight: 600 }}>{t.score}/10</Text>
                      </div>
                      <Progress percent={t.score * 10} strokeColor="#ff4d4f" showInfo={false} size="small" />
                      {t.comment && (
                        <Paragraph style={{ marginTop: 8, marginBottom: 0, color: '#666', fontSize: 13 }}>{t.comment}</Paragraph>
                      )}
                    </div>
                  ))}
                </>
              )}
            </Card>
          )}

          {/* Improvement — 按薄弱点分组展示 */}
          {report.improvement && (
            <Card style={{ marginBottom: 24 }}>
              <Title level={5}><BulbOutlined style={{ color: '#faad14', marginRight: 6 }} />改进建议</Title>
              {weakTopics.length > 0 && (
                <div style={{ marginBottom: 16 }}>
                  <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                    以下建议针对你的薄弱环节：
                  </Text>
                  <Space wrap>
                    {weakTopics.map(t => (
                      <Tag key={t.topic} color="error">{t.topic}</Tag>
                    ))}
                  </Space>
                </div>
              )}
              <Paragraph style={{ whiteSpace: 'pre-wrap', color: '#555', background: '#fafafa', padding: 16, borderRadius: 8 }}>
                {report.improvement}
              </Paragraph>
            </Card>
          )}
        </>
      ) : (
        <Card style={{ marginBottom: 24 }}>
          <Empty description="暂无报告数据" />
        </Card>
      )}

      {/* Actions */}
      <Space style={{ width: '100%', justifyContent: 'center' }} size={16}>
        <Button icon={<ReloadOutlined />} onClick={() => navigate('/setup')}>再来一次</Button>
        <Button type="primary" onClick={() => navigate('/history')}>返回历史</Button>
      </Space>
    </div>
  );
};

export default HistoryDetail;
