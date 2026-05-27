/**
 * 应用根组件 — 路由配置
 * 定义6个页面路由：首页 / 面试设置 / 面试对话 / 面试报告 / 练习历史 / 历史详情
 */
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Setup from './pages/Setup';
import Interview from './pages/Interview';
import Report from './pages/Report';
import History from './pages/History';
import HistoryDetail from './pages/HistoryDetail';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/setup" element={<Setup />} />
        <Route path="/interview/:id" element={<Interview />} />
        <Route path="/report/:id" element={<Report />} />
        <Route path="/history" element={<History />} />
        <Route path="/history/:id" element={<HistoryDetail />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
