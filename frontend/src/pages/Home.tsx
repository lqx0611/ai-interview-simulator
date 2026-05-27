import { useNavigate } from 'react-router-dom';
import { Button, Typography, Space } from 'antd';

const { Title, Paragraph } = Typography;

const Home = () => {
  const navigate = useNavigate();

  return (
    <div style={{ maxWidth: 600, margin: '120px auto', textAlign: 'center', padding: 24 }}>
      <Title level={1}>AI 面试模拟器</Title>
      <Paragraph style={{ fontSize: 16, color: '#666', marginBottom: 32 }}>
        选择你的技术方向，AI 面试官将进行追问式模拟面试，
        <br />
        并根据你的回答质量给出专业评分和改进建议。
      </Paragraph>
      <Space size="middle">
        <Button type="primary" size="large" onClick={() => navigate('/setup')}>
          开始面试
        </Button>
        <Button size="large" onClick={() => navigate('/history')}>
          练习历史
        </Button>
      </Space>
    </div>
  );
};

export default Home;
