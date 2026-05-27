import { useState, useRef, useCallback } from 'react';

/** SSE事件解析结果 */
interface SSEResult {
  topic: string;
  score: number | null;
  action: string;
  finalContent: string;
}

/**
 * SSE（Server-Sent Events）流式对话Hook
 * 使用fetch + ReadableStream消费后端SSE接口，支持逐字接收AI回复
 *
 * @returns streaming 是否正在流式接收中
 * @returns sendMessage 发送消息并开始接收SSE流
 * @returns abort 中断当前SSE连接
 */
export function useSSE() {
  const [streaming, setStreaming] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  /**
   * 发送用户消息并建立SSE连接接收AI流式回复
   *
   * @param url      SSE接口地址
   * @param body     请求体（含用户回答内容）
   * @param onToken  收到每个字符的回调（用于逐字更新UI）
   * @param onDone   收到done事件的回调（本轮对话完成）
   * @param onError  错误回调
   */
  const sendMessage = useCallback(async (
    url: string,
    body: Record<string, unknown>,
    onToken: (text: string) => void,
    onDone: (result: SSEResult) => void,
    onError: (msg: string) => void
  ) => {
    setStreaming(true);
    const controller = new AbortController();
    abortRef.current = controller;

    try {
      // 发起POST请求，Accept设为text/event-stream标识期待SSE响应
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' },
        body: JSON.stringify(body),
        signal: controller.signal,
      });

      if (!response.ok) {
        onError(`请求失败 (${response.status})`);
        return;
      }

      console.log('[SSE] Response received, content-type:', response.headers.get('content-type'));

      // 获取ReadableStream阅读器，手动解析SSE格式
      const reader = response.body?.getReader();
      if (!reader) {
        onError('无法读取响应流');
        return;
      }

      const decoder = new TextDecoder();
      let buffer = '';

      // 循环读取chunk，按行解析SSE的"data:"前缀事件
      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          console.log('[SSE] Stream done, remaining buffer:', buffer);
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        // 最后一行可能不完整，保留到下一次拼接
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed) continue;
          if (!trimmed.startsWith('data:')) continue;

          // 提取 data: 后面的JSON字符串
          const jsonStr = trimmed.slice(5).trim();
          console.log('[SSE] Received event:', jsonStr.substring(0, 80));

          try {
            const data = JSON.parse(jsonStr);
            // 根据event type分发处理
            if (data.type === 'content' && data.text) {
              onToken(data.text as string);
            } else if (data.type === 'done') {
              onDone({
                topic: (data.topic as string) || '',
                score: data.score != null ? (data.score as number) : null,
                action: (data.action as string) || 'next',
                finalContent: (data.final_content as string) || '',
              });
            } else if (data.type === 'error') {
              onError((data.message as string) || '未知错误');
            }
          } catch {
            // JSON解析失败时跳过该行（可能是不完整的数据）
            console.warn('[SSE] Failed to parse JSON:', jsonStr);
          }
        }
      }
    } catch (e: unknown) {
      // AbortError是用户主动取消，不需要报错
      if (e instanceof DOMException && e.name === 'AbortError') {
        return;
      }
      console.error('[SSE] Connection error:', e);
      onError(e instanceof Error ? e.message : '连接失败');
    } finally {
      setStreaming(false);
      abortRef.current = null;
    }
  }, []);

  /** 中断当前SSE连接（切换页面时调用） */
  const abort = useCallback(() => {
    abortRef.current?.abort();
  }, []);

  return { streaming, sendMessage, abort };
}
