/**
 * 用户登录页 — /login
 * 用户名 + 密码 + 登录按钮 + "去注册"链接
 */
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Card, Form, Input, Button, Typography, message, Space } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login } from '../api/auth';
import { saveToken, saveUserInfo } from '../api/request';

const { Title, Text } = Typography;

const LoginPage = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const result = await login(values);
      // Token 存入 Cookie，用户信息存入 localStorage
      saveToken(result.token);
      saveUserInfo(result.user);
      message.success(`欢迎回来，${result.user.nickname}`);
      // 跳转首页
      navigate('/');
    } catch (e: unknown) {
      message.error(e instanceof Error ? e.message : '登录失败');
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
            <Title level={3} style={{ marginBottom: 4 }}>AI 面试模拟器</Title>
            <Text type="secondary">登录后继续你的面试练习</Text>
          </div>

          <Form layout="vertical" onFinish={handleSubmit} autoComplete="off">
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="用户名" size="large" maxLength={20} />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" maxLength={20} />
            </Form.Item>
            <Form.Item style={{ marginBottom: 0 }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="large">
                登录
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">还没有账号？</Text>
            <Link to="/register">去注册</Link>
          </div>
        </Space>
      </Card>
    </div>
  );
};

export default LoginPage;
