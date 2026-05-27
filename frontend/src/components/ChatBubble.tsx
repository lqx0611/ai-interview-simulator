import { useEffect } from 'react';
import { Typography } from 'antd';
import { RobotOutlined, UserOutlined } from '@ant-design/icons';

const { Text } = Typography;

const blinkStyleId = 'chat-bubble-blink';

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
        {streaming && <span className="cursor-blink">|</span>}
      </div>
    </div>
  );
};

export default ChatBubble;
