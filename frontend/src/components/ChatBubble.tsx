/**
 * 对话气泡组件
 * 面试官消息（蓝色机器人头像，灰色气泡，左侧）+ 候选人消息（蓝色用户头像，蓝色气泡，右侧）
 * 支持流式输出时尾部闪烁光标效果
 */
import { useEffect } from 'react';
import { Typography } from 'antd';
import { RobotOutlined, UserOutlined } from '@ant-design/icons';

const { Text } = Typography;

const blinkStyleId = 'chat-bubble-blink';

/** 动态注入闪烁动画CSS（光标样式），只注入一次避免重复添加style标签 */
function ensureBlinkStyle() {
  if (document.getElementById(blinkStyleId)) return;
  const style = document.createElement('style');
  style.id = blinkStyleId;
  style.textContent = `
    @keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
    .cursor-blink { animation: blink 0.8s infinite; }
  `;
  document.head.appendChild(style);
}

interface ChatBubbleProps {
  role: 'interviewer' | 'candidate';
  content: string;
  streaming?: boolean;
}

const avatarStyle: React.CSSProperties = {
  width: 36,
  height: 36,
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: 18,
  flexShrink: 0,
};

const interviewerAvatar: React.CSSProperties = {
  ...avatarStyle,
  background: '#e6f7ff',
  color: '#1890ff',
};

const candidateAvatar: React.CSSProperties = {
  ...avatarStyle,
  background: '#f0f5ff',
  color: '#597ef7',
};

const ChatBubble = ({ role, content, streaming }: ChatBubbleProps) => {
  useEffect(() => { ensureBlinkStyle(); }, []);
  const isInterviewer = role === 'interviewer';

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'flex-start',
        marginBottom: 16,
        gap: 10,
        flexDirection: isInterviewer ? 'row' : 'row-reverse',
      }}
    >
      <div style={isInterviewer ? interviewerAvatar : candidateAvatar}>
        {isInterviewer ? <RobotOutlined /> : <UserOutlined />}
      </div>
      <div
        style={{
          maxWidth: '70%',
          padding: '10px 16px',
          borderRadius: 12,
          background: isInterviewer ? '#f5f5f5' : '#1677ff',
          color: isInterviewer ? '#000' : '#fff',
          lineHeight: 1.7,
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
        }}
      >
        <Text style={{ color: 'inherit' }}>{content}</Text>
        {/* 流式输出中显示闪烁光标，模拟打字效果 */}
        {streaming && <span className="cursor-blink">|</span>}
      </div>
    </div>
  );
};

export default ChatBubble;
