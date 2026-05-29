package com.interview.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 — 从 SecurityContext 中提取当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户的ID
     * JwtAuthenticationFilter 在认证成功后将 userId 存为 Authentication 的 principal
     *
     * @return 当前用户ID，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
