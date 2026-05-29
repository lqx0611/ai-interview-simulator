package com.interview.config;

import com.interview.common.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 在每次请求时从 Authorization Header 提取 Token，验证后设置 SecurityContext
 *
 * 过滤逻辑：
 * 1. 从 Header 提取 Bearer Token → 无 Token 则放行（由 SecurityConfig 白名单/拒绝）
 * 2. Token 有效 → 设置 SecurityContext（后续 Controller 可通过 SecurityContext 获取当前用户）
 * 3. Token 无效 → 放行（由 SecurityConfig 返回 401）
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /** Authorization Header 名称 */
    private static final String AUTH_HEADER = "Authorization";
    /** Bearer Token 前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从 Header 提取 Token
        String token = extractToken(request);

        // 2. Token 有效则设置认证上下文
        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);

            // 构建认证对象（principal=userId，无凭据，无角色）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 继续过滤器链（即使无 Token 也放行，由 SecurityConfig 的 authorizeHttpRequests 决定是否拒绝）
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求 Header 中提取 Bearer Token
     *
     * @return Token 字符串，不存在或不合法时返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
