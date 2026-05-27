import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Tag, Button, Spin, Typography, Row, Col, Progress, Empty, Pagination, Select } from 'antd';
import {
  PlayCircleOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
  WarningOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import { getDashboardStats, getHistory, type DashboardStats, type HistoryItem } from '../api/interview';

const { Title, Text, Paragraph } = Typography;

const LEVEL_CONFIG: Record<string, { label: string; color: string }> = {
  proficient: { label: '精通', color: '#52c41a' },
  skilled: { label: '熟练', color: '#1677ff' },
  familiar: { label: '了解', color: '#faad14' },
  weak: { label: '薄弱', color: '#ff4d4f' },
};

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

const formatDurationLong = (s: number) => {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  if (h > 0) return `${h}小时${m}分钟`;
  if (m > 0) return `${m}分钟`;
  return `${s}秒`;
};

const formatDate = (dateStr: string) => {
  const d = new Date(dateStr);
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  });
};

const Home = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentList, setRecentList] = useState<HistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [topicPage, setTopicPage] = useState(1);
  const [topicPageSize, setTopicPageSize] = useState(5);
  const [topicSortOrder, setTopicSortOrder] = useState<'asc' | 'desc'>('asc');

  useEffect(() => {
    const load = async () => {
      try {
        const [statsRes, historyRes] = await Promise.all([
          getDashboardStats(),
          getHistory(1, 3),
        ]);
        setStats(statsRes);
        setRecentList(historyRes.list);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const sortedTopics = useMemo(() => {
    if (!stats) return [];
    const list = [...stats.topicStats];
    const asc = topicSortOrder === 'asc';
    list.sort((a, b) => asc ? a.avgScore - b.avgScore : b.avgScore - a.avgScore);
    return list;
  }, [stats, topicSortOrder]);

  const topicPageList = useMemo(() => {
    const start = (topicPage - 1) * topicPageSize;
    return sortedTopics.slice(start, start + topicPageSize);
  }, [sortedTopics, topicPage, topicPageSize]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 20px' }}>
      {/* Welcome Area */}
      <div style={{ textAlign: 'center', marginBottom: 48 }}>
        <Title level={1} style={{ marginBottom: 12 }}>AI 面试模拟器</Title>
        <Paragraph style={{ fontSize: 16, color: '#666', marginBottom: 28, maxWidth: 500, marginLeft: 'auto', marginRight: 'auto' }}>
          选择技术方向，AI 面试官将进行追问式模拟面试，根据回答质量给出专业评分和改进建议。
        </Paragraph>
        <Button
          type="primary"
          size="large"
          icon={<PlayCircleOutlined />}
          onClick={() => navigate('/setup')}
          style={{ padding: '6px 40px', height: 48, fontSize: 16, borderRadius: 8 }}
        >
          开始面试
        </Button>
      </div>

      {/* Stats Cards */}
      {stats && (
        <Row gutter={[20, 20]} style={{ marginBottom: 40 }}>
          <Col xs={24} sm={8}>
            <Card hoverable onClick={() => navigate('/history')} style={{ textAlign: 'center', borderRadius: 12 }}>
              <BarChartOutlined style={{ fontSize: 28, color: '#1677ff', marginBottom: 8 }} />
              <div style={{ fontSize: 36, fontWeight: 700, color: '#1677ff', lineHeight: 1.2 }}>
                {stats.totalInterviews}
              </div>
              <Text type="secondary">总面试次数</Text>
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card style={{ textAlign: 'center', borderRadius: 12 }}>
              <ClockCircleOutlined style={{ fontSize: 28, color: '#52c41a', marginBottom: 8 }} />
              <div style={{ fontSize: 30, fontWeight: 700, color: '#52c41a', lineHeight: 1.2 }}>
                {formatDurationLong(stats.totalDurationSeconds)}
              </div>
              <Text type="secondary">总练习时长</Text>
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card style={{ textAlign: 'center', borderRadius: 12 }}>
              <WarningOutlined style={{ fontSize: 28, color: '#ff4d4f', marginBottom: 8 }} />
              <div style={{ fontSize: 36, fontWeight: 700, color: '#ff4d4f', lineHeight: 1.2 }}>
                {stats.weakTopics.length}
              </div>
              <Text type="secondary">薄弱知识点</Text>
            </Card>
          </Col>
        </Row>
      )}

      {/* Knowledge Mastery */}
      {stats && stats.topicStats.length > 0 && (
        <Card
          title={<span style={{ fontSize: 18, fontWeight: 600 }}>知识点掌握度</span>}
          extra={
            <span
              style={{ cursor: 'pointer', color: '#1677ff', fontSize: 16 }}
              onClick={() => { setTopicSortOrder(o => o === 'asc' ? 'desc' : 'asc'); setTopicPage(1); }}
              title={topicSortOrder === 'asc' ? '升序' : '降序'}
            >
              {topicSortOrder === 'asc' ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
            </span>
          }
          style={{ marginBottom: 32, borderRadius: 12 }}
        >
          {topicPageList.map((t, i) => {
            const config = LEVEL_CONFIG[t.level] || LEVEL_CONFIG.familiar;
            return (
              <div
                key={t.topic}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 16,
                  padding: '12px 0',
                  borderBottom: i < topicPageList.length - 1 ? '1px solid #f0f0f0' : 'none',
                }}
              >
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <Text strong style={{ fontSize: 15 }}>{t.topic}</Text>
                    <Tag color={config.color} style={{ margin: 0 }}>{config.label}</Tag>
                  </div>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    练习 {t.practiceCount} 次 · 最高 {t.maxScore} · 最低 {t.minScore}
                  </Text>
                </div>
                <div style={{ width: 160, textAlign: 'right' }}>
                  <Progress
                    percent={Math.round(t.avgScore * 10)}
                    size="small"
                    strokeColor={config.color}
                    format={() => `${t.avgScore}`}
                    style={{ marginBottom: 0 }}
                  />
                </div>
              </div>
            );
          })}
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 12, marginTop: 16 }}>
            <Pagination
              current={topicPage}
              pageSize={topicPageSize}
              total={sortedTopics.length}
              onChange={setTopicPage}
              size="small"
              showTotal={t => `共 ${t} 个知识点`}
            />
            <Select
              value={topicPageSize}
              onChange={v => { setTopicPageSize(v); setTopicPage(1); }}
              size="small"
              style={{ width: 100 }}
              options={[
                { value: 5, label: '5 条/页' },
                { value: 10, label: '10 条/页' },
                { value: 20, label: '20 条/页' },
              ]}
            />
          </div>
        </Card>
      )}

      {/* Recent History */}
      <Card
        title={<span style={{ fontSize: 18, fontWeight: 600 }}>最近面试</span>}
        extra={recentList.length > 0 ? <a onClick={() => navigate('/history')}>查看全部</a> : undefined}
        style={{ borderRadius: 12 }}
      >
        {recentList.length === 0 ? (
          <Empty description="暂无练习记录">
            <Button type="primary" onClick={() => navigate('/setup')}>开始第一次面试</Button>
          </Empty>
        ) : (
          recentList.map((item, i) => (
            <div
              key={item.id}
              onClick={() => navigate(`/history/${item.id}`)}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '12px 0',
                borderBottom: i < recentList.length - 1 ? '1px solid #f0f0f0' : 'none',
                cursor: 'pointer',
              }}
            >
              <div>
                <div style={{ marginBottom: 4 }}>
                  <Tag color={DIR_COLORS[item.direction] || 'default'}>
                    {DIR_LABELS[item.direction] || item.direction}
                  </Tag>
                  <Tag>{DIFF_LABELS[item.difficulty] || item.difficulty}</Tag>
                </div>
                <Text type="secondary" style={{ fontSize: 12 }}>{formatDate(item.createTime)}</Text>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: 20, fontWeight: 600, color: '#1677ff' }}>
                  {item.totalScore ?? '-'}
                </div>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {formatDuration(item.durationSeconds)} · {item.questionCount} 题
                </Text>
              </div>
            </div>
          ))
        )}
      </Card>
    </div>
  );
};

export default Home;
