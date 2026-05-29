package com.interview.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 — 负责 Token 的生成、解析和校验
 *
 * 使用 HMAC-SHA256 算法签名，密钥和有效期从 application.yml 注入
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-days}")
    private long expirationDays;

    /** 获取HMAC-SHA256签名密钥 */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID，存入 subject 字段
     * @return 签发的 JWT 字符串
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationDays * 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT 字符串
     * @return 用户ID
     * @throws JwtException Token无效或过期时抛出
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token JWT 字符串
     * @return true=有效，false=无效或过期
     */
    public boolean validateToken(String token) {
        try {
            getUserId(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
