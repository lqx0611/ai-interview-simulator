/**
 * 面试对话页 — 核心页面
 * 展示AI面试官和用户的对话，支持SSE流式输出（打字机效果）
 * 用户输入回答后通过SSE接收AI的逐字回复，可随时结束面试
 *
 * 页面刷新安全：挂载时自动从后端拉取面试详情，还原对话记录和计时器
 * 不依赖 location.state（刷新后丢失），完全基于URL中的interview id恢复状态
 */
import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Input, Tag, Modal, message, Space, Spin } from 'antd';
import ChatBubble from '../components/ChatBubble';
import UserMenu from '../components/UserMenu';
import { useSSE } from '../hooks/useSSE';
import { endInterview, getInterviewDetail } from '../api/interview';

const DIR_LABELS: Record<string, string> = {
  java_backend: 'Java后端', ai_dev: 'AI开发', fullstack: '全栈开发',
};

interface ChatMessage {
  id: string;
  role: 'interviewer' | 'candidate';
  content: string;
  streaming?: boolean;
}

let msgId = 0;

const Interview = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { streaming, sendMessage } = useSSE();

  // ── 页面恢复相关状态 ──
  const [loading, setLoading] = useState(true);
  const [direction, setDirection] = useState<string>('java_backend');

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [ending, setEnding] = useState(false);
  const [seconds, setSeconds] = useState(0);
  const [questionCount, setQuestionCount] = useState(1);
  /** 面试开始时间（ISO字符串），用于页面刷新后恢复计时器 */
  const createTimeRef = useRef<string | null>(null);

  const chatEndRef = useRef<HTMLDivElement>(null);

  /**
   * 挂载时从后端加载面试详情，还原对话记录和面试状态
   * 这样即使页面刷新，也能从后端恢复完整的对话历史
   */
  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const detail = await getInterviewDetail(Number(id));

        // 面试已完成，直接跳转报告页
        if (detail.totalScore != null) {
          navigate(`/report/${id}`, { replace: true });
          return;
        }

        if (cancelled) return;

        // 还原面试方向
        setDirection(detail.direction);

        // 还原提问计数
        setQuestionCount(detail.questionCount);

        // 恢复对话消息（含AI和用户的完整历史）
        if (detail.messages && detail.messages.length > 0) {
          const restored: ChatMessage[] = detail.messages.map(m => ({
            id: String(++msgId),
            role: m.role as 'interviewer' | 'candidate',
            content: m.content,
            streaming: false,
          }));
          setMessages(restored);
        }

        // 根据面试创建时间计算已流失秒数，继续计时
        if (detail.createTime) {
          createTimeRef.current = detail.createTime;
          const elapsed = Math.floor((Date.now() - new Date(detail.createTime).getTime()) / 1000);
          setSeconds(Math.max(0, elapsed));
        }
      } catch {
        message.error('加载面试数据失败');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, [id, navigate]);

  /** 面试计时器：从面试开始时间持续递增（页面刷新后从已流失时间接续） */
  useEffect(() => {
    const timer = setInterval(() => setSeconds(s => s + 1), 1000);
    return () => clearInterval(timer);
  }, []);

  /** 新消息到达时自动滚动到底部 */
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const formatTime = (s: number) => {
    const m = Math.floor(s / 60).toString().padStart(2, '0');
    const sec = (s % 60).toString().padStart(2, '0');
    return `${m}:${sec}`;
  };

  /** 发送用户回答，通过SSE接收AI追问/回复 */
  const handleSend = useCallback(() => {
    const content = inputValue.trim();
    if (!content || streaming) return;

    // 添加用户消息到对话列表
    const userMsg: ChatMessage = { id: String(++msgId), role: 'candidate', content };
    setMessages(prev => [...prev, userMsg]);
    setInputValue('');

    // 创建AI消息占位，流式回调中逐字填充
    const aiMsgId = String(++msgId);
    const aiMsg: ChatMessage = { id: aiMsgId, role: 'interviewer', content: '', streaming: true };
    setMessages(prev => [...prev, aiMsg]);

    sendMessage(
      `/api/interview/${id}/answer`,
      { content },
      (token) => {
        setMessages(prev => prev.map(m =>
          m.id === aiMsgId ? { ...m, content: m.content + token } : m
        ));
      },
      (result) => {
        setMessages(prev => prev.map(m =>
          m.id === aiMsgId ? { ...m, streaming: false } : m
        ));
        setQuestionCount(q => q + 1);
        // AI建议结束面试时自动触发结束流程
        if (result.action === 'end') {
          handleEndInterview();
        }
      },
      (err) => {
        setMessages(prev => prev.map(m =>
          m.id === aiMsgId ? { ...m, streaming: false, content: `[错误: ${err}]` } : m
        ));
        message.error(err);
      }
    );
  }, [inputValue, streaming, id, sendMessage]);

  /** 调用后端结束面试API，跳转到报告页 */
  const handleEndInterview = async () => {
    if (ending) return;
    setEnding(true);
    try {
      const result = await endInterview(Number(id));
      navigate(`/report/${id}`, { state: { report: result } });
    } catch {
      message.error('结束面试失败');
    } finally {
      setEnding(false);
    }
  };

  const confirmEnd = () => {
    Modal.confirm({
      title: '确认结束面试？',
      content: '结束后将生成面试评估报告，无法继续当前对话。',
      okText: '确认结束',
      cancelText: '继续面试',
      onOk: handleEndInterview,
    });
  };

  // 加载中显示Spin，等待后端数据恢复
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载面试数据..." />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', maxWidth: 800, margin: '0 auto' }}>
      {/* Top bar */}
      <div className="interview-topbar" style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '12px 20px', borderBottom: '1px solid #f0f0f0', background: '#fff',
        flexShrink: 0, flexWrap: 'wrap', gap: 6,
      }}>
        <Space>
          <UserMenu />
          <Tag color="blue">{DIR_LABELS[direction] || direction}</Tag>
          <span style={{ color: '#999' }}>提问 {questionCount} 次</span>
        </Space>
        <Space>
          <span style={{ fontFamily: 'monospace', fontSize: 16 }}>{formatTime(seconds)}</span>
          <Button danger size="small" onClick={confirmEnd} loading={ending}>结束面试</Button>
        </Space>
      </div>

      {/* Chat area */}
      <div className="interview-chat-area" style={{
        flex: 1, overflowY: 'auto', padding: '20px 16px',
        background: '#fafafa',
      }}>
        {messages.map(msg => (
          <ChatBubble key={msg.id} role={msg.role} content={msg.content} streaming={msg.streaming} />
        ))}
        <div ref={chatEndRef} />
      </div>

      {/* Input area */}
      <div className="interview-input-area" style={{
        padding: '12px 16px', borderTop: '1px solid #f0f0f0', background: '#fff',
        display: 'flex', gap: 10, alignItems: 'flex-end', flexShrink: 0,
      }}>
        <Input.TextArea
          value={inputValue}
          onChange={e => setInputValue(e.target.value)}
          onPressEnter={e => {
            if (!e.shiftKey) {
              e.preventDefault();
              handleSend();
            }
          }}
          placeholder="输入你的回答... (Enter 发送, Shift+Enter 换行, 最多2000字)"
          rows={3}
          maxLength={2000}
          showCount
          disabled={streaming}
          style={{ flex: 1 }}
        />
        <Button type="primary" onClick={handleSend} loading={streaming} disabled={!inputValue.trim()}>
          发送
        </Button>
      </div>
    </div>
  );
};

export default Interview;
