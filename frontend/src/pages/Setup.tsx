import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Radio, Button, Typography, Space, message } from 'antd';
import { startInterview } from '../api/interview';

const { Title } = Typography;

const directionOptions = [
  { label: 'Java 后端', value: 'java_backend' },
  { label: 'AI 开发', value: 'ai_dev' },
  { label: '全栈开发', value: 'fullstack' },
];

const difficultyOptions = [
  { label: '初级', value: 'junior' },
  { label: '中级', value: 'mid' },
  { label: '高级', value: 'senior' },
];

const typeOptions = [
  { label: '知识点深挖', value: 'knowledge' },
  { label: '项目经验追问', value: 'project' },
  { label: '综合面试', value: 'comprehensive' },
];

const Setup = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const handleStart = async (values: { direction: string; difficulty: string; interviewType: string }) => {
    setLoading(true);
    try {
      const result = await startInterview(values);
      navigate(`/interview/${result.interviewId}`, {
        state: { openingMessage: result.openingMessage, direction: values.direction },
      });
    } catch {
      message.error('开始面试失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: '60px auto', padding: 24 }}>
      <Title level={2} style={{ textAlign: 'center', marginBottom: 32 }}>
        AI 面试模拟器
      </Title>
      <Card>
        <Form
          form={form}
          layout="vertical"
          initialValues={{ direction: 'java_backend', difficulty: 'mid', interviewType: 'knowledge' }}
          onFinish={handleStart}
        >
          <Form.Item label="面试方向" name="direction" rules={[{ required: true }]}>
            <Radio.Group options={directionOptions} />
          </Form.Item>

          <Form.Item label="难度" name="difficulty" rules={[{ required: true }]}>
            <Radio.Group options={difficultyOptions} />
          </Form.Item>

          <Form.Item label="面试类型" name="interviewType" rules={[{ required: true }]}>
            <Radio.Group options={typeOptions} />
          </Form.Item>

          <Form.Item style={{ textAlign: 'center', marginTop: 24 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} size="large">
                开始面试
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Setup;
