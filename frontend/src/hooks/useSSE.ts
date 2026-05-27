import { useState, useRef, useCallback } from 'react';

interface SSEResult {
  topic: string;
  score: number | null;
  action: string;
  finalContent: string;
}

export function useSSE() {
  const [streaming, setStreaming] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

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

      const reader = response.body?.getReader();
      if (!reader) {
        onError('无法读取响应流');
        return;
      }

      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          console.log('[SSE] Stream done, remaining buffer:', buffer);
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed) continue;
          if (!trimmed.startsWith('data:')) continue;

          const jsonStr = trimmed.slice(5).trim();
          console.log('[SSE] Received event:', jsonStr.substring(0, 80));

          try {
            const data = JSON.parse(jsonStr);
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
            console.warn('[SSE] Failed to parse JSON:', jsonStr);
          }
        }
      }
    } catch (e: unknown) {
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

  const abort = useCallback(() => {
    abortRef.current?.abort();
  }, []);

  return { streaming, sendMessage, abort };
}
