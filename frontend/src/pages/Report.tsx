import { useLocation, useNavigate } from 'react-router-dom';
import { Button, Card, Progress, Space, Tag, Typography, Divider, Empty } from 'antd';
import {
  ClockCircleOutlined, QuestionCircleOutlined, TrophyOutlined,
  WarningOutlined, BulbOutlined, ReloadOutlined, HomeOutlined
} from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

const formatDuration = (s: number) => {
  const m = Math.floor(s / 60);
  const sec = s % 60;
  if (m > 0) return `${m}分${sec}秒`;
  return `${sec}秒`;
};

const scoreColor = (score: number) => {
  if (score >= 8) return '#52c41a';
  if (score >= 6) return '#1677ff';
  if (score >= 4) return '#faad14';
  return '#ff4d4f';
};

const scoreLevel = (score: number) => {
  if (score >= 9) return '优秀';
  if (score >= 7) return '良好';
  if (score >= 5) return '一般';
  return '需要提升';
};

const Report = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const report = location.state?.report as import('../api/interview').EndInterviewResult | null;

  if (!report) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Empty description="暂无报告数据，请通过正常流程完成面试">
          <Button type="primary" onClick={() => navigate('/')}>返回首页</Button>
        </Empty>
      </div>
    );
  }

  const weakTopics = report.topicScores.filter(t => t.isWeak);
  const strongTopics = report.topicScores.filter(t => !t.isWeak);

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '32px 20px' }}>
      {/* Overall Score */}
      <Card style={{ textAlign: 'center', marginBottom: 24 }}>
        <Title level={4} type="secondary" style={{ marginBottom: 16 }}>面试评估报告</Title>
        <div style={{ position: 'relative', display: 'inline-block' }}>
          <Progress
            type="circle"
            percent={report.overallScore * 10}
            size={180}
            strokeColor={scoreColor(report.overallScore)}
            format={() => (
              <div>
                <div style={{ fontSize: 48, fontWeight: 700, color: scoreColor(report.overallScore), lineHeight: 1 }}>
                  {report.overallScore}
                </div>
                <div style={{ fontSize: 14, color: '#999' }}>/ 10</div>
              </div>
            )}
          />
        </div>
        <div style={{ marginTop: 12 }}>
          <Tag color={scoreColor(report.overallScore)} style={{ fontSize: 16, padding: '2px 16px' }}>
            {scoreLevel(report.overallScore)}
          </Tag>
        </div>
      </Card>

      {/* Stats */}
      <Card style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-around', textAlign: 'center' }}>
          <div>
            <ClockCircleOutlined style={{ fontSize: 24, color: '#1677ff', marginBottom: 8 }} />
            <div style={{ fontSize: 20, fontWeight: 600 }}>{formatDuration(report.durationSeconds)}</div>
            <Text type="secondary">面试时长</Text>
          </div>
          <div>
            <QuestionCircleOutlined style={{ fontSize: 24, color: '#1677ff', marginBottom: 8 }} />
            <div style={{ fontSize: 20, fontWeight: 600 }}>{report.questionCount}</div>
            <Text type="secondary">提问次数</Text>
          </div>
          <div>
            <TrophyOutlined style={{ fontSize: 24, color: '#1677ff', marginBottom: 8 }} />
            <div style={{ fontSize: 20, fontWeight: 600 }}>{report.topicScores.length}</div>
            <Text type="secondary">覆盖知识点</Text>
          </div>
        </div>
      </Card>

      {/* Summary */}
      <Card style={{ marginBottom: 24 }}>
        <Title level={5}>整体评价</Title>
        <Paragraph style={{ whiteSpace: 'pre-wrap', color: '#555' }}>{report.summary}</Paragraph>
      </Card>

      {/* Topic Scores */}
      <Card style={{ marginBottom: 24 }}>
        <Title level={5}>
          知识点评分
          {weakTopics.length > 0 && (
            <Tag color="error" style={{ marginLeft: 8 }}>{weakTopics.length} 个薄弱点</Tag>
          )}
        </Title>

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
                  <Progress
                    percent={t.score * 10}
                    strokeColor={scoreColor(t.score)}
                    showInfo={false}
                    size="small"
                  />
                </div>
              </div>
            ))}
          </>
        )}

        {weakTopics.length > 0 && strongTopics.length > 0 && <Divider style={{ margin: '8px 0 16px' }} />}

        {weakTopics.length > 0 && (
          <>
            <Text type="danger" style={{ display: 'block', marginBottom: 12 }}>
              <WarningOutlined /> 薄弱环节
            </Text>
            {weakTopics.map(t => (
              <div key={t.topic} style={{
                background: '#fff2f0', border: '1px solid #ffccc7', borderRadius: 8,
                padding: '12px 16px', marginBottom: 12,
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <Text strong style={{ color: '#ff4d4f' }}>{t.topic}</Text>
                  <Text style={{ color: '#ff4d4f', fontWeight: 600 }}>{t.score}/10</Text>
                </div>
                <Progress
                  percent={t.score * 10}
                  strokeColor="#ff4d4f"
                  showInfo={false}
                  size="small"
                />
                {t.comment && (
                  <Paragraph style={{ marginTop: 8, marginBottom: 0, color: '#666', fontSize: 13 }}>
                    {t.comment}
                  </Paragraph>
                )}
              </div>
            ))}
          </>
        )}
      </Card>

      {/* Improvement */}
      {report.improvement && (
        <Card style={{ marginBottom: 24 }}>
          <Title level={5}>
            <BulbOutlined style={{ color: '#faad14', marginRight: 6 }} />
            改进建议
          </Title>
          <Paragraph style={{ whiteSpace: 'pre-wrap', color: '#555' }}>{report.improvement}</Paragraph>
        </Card>
      )}

      {/* Actions */}
      <Space style={{ width: '100%', justifyContent: 'center' }} size={16}>
        <Button icon={<ReloadOutlined />} onClick={() => navigate('/setup')}>
          再来一次
        </Button>
        <Button type="primary" icon={<HomeOutlined />} onClick={() => navigate('/')}>
          返回首页
        </Button>
      </Space>
    </div>
  );
};

export default Report;
