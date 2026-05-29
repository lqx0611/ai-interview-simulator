/**
 * 用户注册页 — /register
 * 用户名 + 密码 + 昵称（可选）+ 注册按钮 + "去登录"链接
 */
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Card, Form, Input, Button, Typography, message, Space } from 'antd';
import { UserOutlined, LockOutlined, SmileOutlined } from '@ant-design/icons';
import { register } from '../api/auth';

const { Title, Text } = Typography;

const RegisterPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (values: { username: string; password: string; nickname?: string }) => {
    setLoading(true);
    try {
      await register(values);
      message.success('注册成功，请登录');
      // 跳转登录页
      navigate('/login');
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '注册失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    }}>
      <Card style={{ width: 400, borderRadius: 12, boxShadow: '0 8px 24px rgba(0,0,0,0.15)' }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>
            <Title level={3} style={{ marginBottom: 4 }}>创建新账号</Title>
            <Text type="secondary">注册后即可开始 AI 模拟面试</Text>
          </div>

          <Form layout="vertical" onFinish={handleSubmit} autoComplete="off">
            <Form.Item
              name="username"
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, message: '用户名至少3个字符' },
                { max: 20, message: '用户名最长20个字符' },
              ]}
            >
              <Input prefix={<UserOutlined />} placeholder="用户名（3-20字符）" size="large" maxLength={20} />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6个字符' },
                { max: 20, message: '密码最长20个字符' },
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="密码（6-20字符）" size="large" maxLength={20} />
            </Form.Item>
            <Form.Item
              name="nickname"
              rules={[{ max: 50, message: '昵称最长50个字符' }]}
            >
              <Input prefix={<SmileOutlined />} placeholder="昵称（可选，默认使用用户名）" size="large" maxLength={50} />
            </Form.Item>
            <Form.Item style={{ marginBottom: 0 }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="large">
                注册
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">已有账号？</Text>
            <Link to="/login">去登录</Link>
          </div>
        </Space>
      </Card>
    </div>
  );
};

export default RegisterPage;
