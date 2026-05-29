/**
 * 应用根组件 — 路由配置
 * 定义8个页面路由：首页/面试设置/面试对话/面试报告/练习历史/历史详情/登录/注册
 *
 * 登录/注册页：独立全屏背景，不使用AppLayout
 * 面试对话页：有独立的面试顶栏（计时器+结束按钮），不使用AppLayout
 * 其余业务页面：包裹在AppLayout中，共享顶部导航栏（含用户菜单）
 */
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import Home from './pages/Home';
import Setup from './pages/Setup';
import Interview from './pages/Interview';
import Report from './pages/Report';
import History from './pages/History';
import HistoryDetail from './pages/HistoryDetail';
import Login from './pages/Login';
import Register from './pages/Register';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 登录/注册页：独立样式，无AppLayout顶栏 */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* 面试对话页：有自己的顶栏（计时器+结束按钮+用户菜单），不在AppLayout内 */}
        <Route path="/interview/:id" element={<Interview />} />

        {/* 其余业务页面：统一顶栏（App标题 + 用户菜单） */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/setup" element={<Setup />} />
          <Route path="/report/:id" element={<Report />} />
          <Route path="/history" element={<History />} />
          <Route path="/history/:id" element={<HistoryDetail />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
