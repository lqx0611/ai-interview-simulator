package com.interview.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置类
 * 配置 JWT 无状态认证：放行白名单路径，其余接口需携带合法 Token
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** 白名单路径 — 无需登录即可访问 */
    private static final String[] WHITELIST = {
            "/api/auth/**",     // 注册、登录接口
            "/actuator/**"      // 健康检查、指标端点
    };

    /**
     * BCrypt 密码编码器
     * 用于注册时对用户密码进行不可逆加密存储
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链
     * 配置无状态JWT认证：白名单放行 + JWT过滤器 + 401 JSON响应
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            // 禁用CSRF（前后端分离 + JWT，不需要CSRF保护）
            .csrf(csrf -> csrf.disable())
            // 无状态会话（JWT自带身份信息，不需要服务端Session）
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 路径访问控制
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITELIST).permitAll()
                    .anyRequest().authenticated())
            // 未登录或Token无效时返回401 JSON（而非默认的302重定向到登录页）
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, resp, e) -> write401(resp, "未登录或Token已过期")))
            // 在UsernamePasswordAuthenticationFilter之前插入JWT过滤器
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 向客户端写入 401 JSON 错误响应 */
    private void write401(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            Result<Void> result = Result.error(401, message);
            PrintWriter writer = response.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(result));
            writer.flush();
        } catch (Exception ignored) {
            // 响应已断开，无法写入
        }
    }
}
