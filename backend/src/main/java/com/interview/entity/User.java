package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 — 映射 sys_user 表
 * 存储用户基本身份信息（Phase 1暂不做登录，所有数据关联默认用户 user_id=1）
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录用） */
    private String username;

    /** 密码（BCrypt加密） */
    private String password;

    /** 昵称（界面展示用） */
    private String nickname;

    /** 账号创建时间 */
    private LocalDateTime createTime;

    /** 最近更新时间 */
    private LocalDateTime updateTime;
}
