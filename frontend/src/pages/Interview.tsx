import { useParams } from 'react-router-dom';

const Interview = () => {
  const { id } = useParams<{ id: string }>();
  return <div style={{ padding: 24 }}><h1>面试对话</h1><p>面试 ID: {id} - 待开发</p></div>;
};

export default Interview;
