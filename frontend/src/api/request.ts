import axios from 'axios';

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

request.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.code !== 200) {
      console.error(`API Error [${data.code}]: ${data.message}`);
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return data;
  },
  (error) => {
    console.error('Network error:', error.message);
    return Promise.reject(error);
  }
);

export default request;
