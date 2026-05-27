import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Tag, Pagination, Select, Spin, Empty, Typography } from 'antd';
import { ArrowLeftOutlined, ClockCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { getHistory, type HistoryItem, type PageResponse } from '../api/interview';

const { Title, Text } = Typography;

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

const History = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<PageResponse<HistoryItem> | null>(null);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [pageSize, setPageSize] = useState(5);

  const fetchData = async (p: number, ps: number) => {
    setLoading(true);
    try {
      const result = await getHistory(p, ps);
      setData(result);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(page, pageSize);
  }, [page, pageSize]);

  if (loading && !data) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (data && data.total === 0) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Empty description="暂无练习记录">
          <a onClick={() => navigate('/setup')}>开始第一次面试</a>
        </Empty>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '32px 20px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <ArrowLeftOutlined
          onClick={() => navigate('/')}
          style={{ fontSize: 18, cursor: 'pointer', color: '#1677ff' }}
        />
        <Title level={3} style={{ margin: 0 }}>练习历史</Title>
      </div>

      <Spin spinning={loading && !!data}>
        {data?.list.map(item => (
          <Card
            key={item.id}
            hoverable
            onClick={() => navigate(`/history/${item.id}`)}
            style={{ marginBottom: 16 }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <div style={{ marginBottom: 8 }}>
                  <Tag color={DIR_COLORS[item.direction] || 'default'}>
                    {DIR_LABELS[item.direction] || item.direction}
                  </Tag>
                  <Tag>{DIFF_LABELS[item.difficulty] || item.difficulty}</Tag>
                </div>
                <div style={{ display: 'flex', gap: 24, color: '#999', fontSize: 13 }}>
                  <span><ClockCircleOutlined style={{ marginRight: 4 }} />{formatDuration(item.durationSeconds)}</span>
                  <span><QuestionCircleOutlined style={{ marginRight: 4 }} />{item.questionCount} 题</span>
                </div>
              </div>
              <div style={{ textAlign: 'center', minWidth: 80 }}>
                <div style={{ fontSize: 36, fontWeight: 700, color: '#1677ff', lineHeight: 1 }}>
                  {item.totalScore ?? '-'}
                </div>
                <Text type="secondary">评分</Text>
              </div>
            </div>
            <Text type="secondary" style={{ fontSize: 12 }}>{formatDate(item.createTime)}</Text>
          </Card>
        ))}
      </Spin>

      {data && data.total > 0 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 12, marginTop: 24 }}>
          <Pagination
            current={page}
            pageSize={pageSize}
            total={data.total}
            onChange={setPage}
            showTotal={t => `共 ${t} 条记录`}
          />
          <Select
            value={pageSize}
            onChange={v => { setPageSize(v); setPage(1); }}
            style={{ width: 100 }}
            options={[
              { value: 5, label: '5 条/页' },
              { value: 10, label: '10 条/页' },
              { value: 20, label: '20 条/页' },
              { value: 50, label: '50 条/页' },
              { value: 100, label: '100 条/页' },
            ]}
          />
        </div>
      )}
    </div>
  );
};

export default History;
