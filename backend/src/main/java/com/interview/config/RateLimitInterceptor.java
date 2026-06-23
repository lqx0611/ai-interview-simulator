package com.interview.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.RateLimit;
import com.interview.common.Result;
import com.interview.common.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 * 基于Redis计数器实现：首次请求写入key并设TTL，后续请求递增，超限返回429
 *
 * Redis Key格式：rate_limit:{userId}:{requestURI}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查方法是否有@RateLimit注解
        RateLimit limit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (limit == null) {
            return true;
        }

        // 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return true; // 未登录用户由SecurityConfig处理401
        }

        String key = "rate_limit:" + userId + ":" + request.getRequestURI();

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            // 首次请求设置过期时间
            if (count != null && count == 1) {
                redisTemplate.expire(key, limit.windowSeconds(), TimeUnit.SECONDS);
            }

            if (count != null && count > limit.maxCalls()) {
                log.warn("Rate limit exceeded: userId={}, uri={}, count={}", userId, request.getRequestURI(), count);
                write429(response, limit.message());
                return false;
            }
        } catch (Exception e) {
            // Redis 不可用时优雅降级：放行请求，由业务层兜底
            // 限流是辅助功能，不能因为 Redis 故障导致整个服务不可用
            log.error("Redis unavailable, rate limit bypassed: userId={}, uri={}", userId, request.getRequestURI(), e);
        }

        return true;
    }

    /** 向客户端写入 429 Too Many Requests JSON 错误响应 */
    private void write429(HttpServletResponse response, String message) {
        try {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(Result.error(429, message)));
            writer.flush();
        } catch (Exception ignored) {
            // 响应已断开
        }
    }
}
