package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试消息实体 — 映射 message 表
 * 存储面试对话中每一轮消息（含AI提问和用户回答）
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属面试记录ID */
    private Long interviewId;

    /** 消息角色：interviewer(AI面试官) / candidate(候选人) */
    private String role;

    /** 消息正文内容 */
    private String content;

    /** 当前考察的知识点名称（由AI提取，仅interviewer消息有值） */
    private String topic;

    /** AI对该回答的评分（1-10分，仅candidate消息有值） */
    private BigDecimal score;

    /** 消息时间 */
    private LocalDateTime createTime;
}
