package com.interview.dto;

import com.interview.entity.User;
import lombok.Data;

/**
 * 用户信息响应 — 返回用户基本信息（不含密码、时间戳等敏感或冗余字段）
 */
@Data
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;

    /** 从User实体构建响应对象 */
    public static UserResponse from(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        return resp;
    }
}
