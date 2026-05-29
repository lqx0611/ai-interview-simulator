/**
 * 用户菜单组件
 * 显示用户头像（默认图标）+ 昵称 + 退出登录按钮
 * 点击头像弹出 Popover 展示用户详细信息
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Avatar, Button, Popover, Space, Typography, message, Descriptions } from 'antd';
import { UserOutlined, LogoutOutlined } from '@ant-design/icons';
import { getUserInfo, logout } from '../api/request';

const { Text } = Typography;

const UserMenu = () => {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const user = getUserInfo();

  const handleLogout = () => {
    logout();
    message.success('已退出登录');
    navigate('/login');
  };

  if (!user) return null;

  const popoverContent = (
    <div style={{ width: 220 }}>
      <Descriptions column={1} size="small" style={{ marginBottom: 12 }}>
        <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
        <Descriptions.Item label="昵称">{user.nickname}</Descriptions.Item>
        <Descriptions.Item label="用户ID">{user.id}</Descriptions.Item>
      </Descriptions>
      <Button type="primary" danger block icon={<LogoutOutlined />} onClick={handleLogout}>
        退出登录
      </Button>
    </div>
  );

  return (
    <Popover
      content={popoverContent}
      title="用户信息"
      trigger="click"
      open={open}
      onOpenChange={setOpen}
      placement="bottomRight"
      overlayStyle={{ maxWidth: 260 }}
    >
      <Space style={{ cursor: 'pointer' }} onClick={() => setOpen(!open)}>
        <Avatar style={{ backgroundColor: '#1677ff' }} icon={<UserOutlined />} />
        <Text strong style={{ color: '#333' }}>{user.nickname}</Text>
      </Space>
    </Popover>
  );
};

export default UserMenu;
