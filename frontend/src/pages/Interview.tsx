import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Input, Tag, Modal, message, Space } from 'antd';
import ChatBubble from '../components/ChatBubble';
import { useSSE } from '../hooks/useSSE';
import { endInterview } from '../api/interview';

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
  const location = useLocation();
  const navigate = useNavigate();
  const { streaming, sendMessage } = useSSE();

  const state = location.state as { openingMessage?: string; direction?: string } | null;
  const direction = state?.direction || 'java_backend';

  const [messages, setMessages] = useState<ChatMessage[]>(() => {
    if (state?.openingMessage) {
      return [{ id: String(++msgId), role: 'interviewer', content: state.openingMessage }];
    }
    return [];
  });
  const [inputValue, setInputValue] = useState('');
  const [ending, setEnding] = useState(false);
  const [seconds, setSeconds] = useState(0);
  const [questionCount, setQuestionCount] = useState(1);

  const chatEndRef = useRef<HTMLDivElement>(null);
  // Timer
  useEffect(() => {
    const timer = setInterval(() => setSeconds(s => s + 1), 1000);
    return () => clearInterval(timer);
  }, []);

  // Auto-scroll
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const formatTime = (s: number) => {
    const m = Math.floor(s / 60).toString().padStart(2, '0');
    const sec = (s % 60).toString().padStart(2, '0');
    return `${m}:${sec}`;
  };

  const handleSend = () => {
    const content = inputValue.trim();
    if (!content || streaming) return;

    const userMsg: ChatMessage = { id: String(++msgId), role: 'candidate', content };
    setMessages(prev => [...prev, userMsg]);
    setInputValue('');

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
        // If AI suggests ending, auto-end
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
  };

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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', maxWidth: 800, margin: '0 auto' }}>
      {/* Top bar */}
      <div style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '12px 20px', borderBottom: '1px solid #f0f0f0', background: '#fff',
        flexShrink: 0,
      }}>
        <Space>
          <Tag color="blue">{DIR_LABELS[direction] || direction}</Tag>
          <span style={{ color: '#999' }}>提问 {questionCount} 次</span>
        </Space>
        <Space>
          <span style={{ fontFamily: 'monospace', fontSize: 16 }}>{formatTime(seconds)}</span>
          <Button danger size="small" onClick={confirmEnd} loading={ending}>结束面试</Button>
        </Space>
      </div>

      {/* Chat area */}
      <div style={{
        flex: 1, overflowY: 'auto', padding: '20px 16px',
        background: '#fafafa',
      }}>
        {messages.map(msg => (
          <ChatBubble key={msg.id} role={msg.role} content={msg.content} streaming={msg.streaming} />
        ))}
        <div ref={chatEndRef} />
      </div>

      {/* Input area */}
      <div style={{
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
          placeholder="输入你的回答... (Enter 发送, Shift+Enter 换行)"
          rows={3}
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
