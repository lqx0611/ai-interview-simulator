/**
 * 应用入口
 * 挂载React应用到DOM，使用StrictMode开启开发期严格检查
 */
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
