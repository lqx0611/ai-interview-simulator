/**
 * 应用布局组件
 * 为所有业务页面提供统一的顶部导航栏（含用户菜单）
 * 使用 React Router <Outlet /> 渲染子路由内容
 */
import { Outlet } from 'react-router-dom';
import UserMenu from './UserMenu';
import { Typography } from 'antd';

const { Text } = Typography;

const AppLayout = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {/* 顶部导航栏 */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '0 24px',
        height: 56,
        borderBottom: '1px solid #f0f0f0',
        background: '#fff',
        flexShrink: 0,
      }}>
        <Text strong style={{ fontSize: 16 }}>AI 面试模拟器</Text>
        <UserMenu />
      </div>

      {/* 页面内容区 */}
      <div style={{ flex: 1 }}>
        <Outlet />
      </div>
    </div>
  );
};

export default AppLayout;
