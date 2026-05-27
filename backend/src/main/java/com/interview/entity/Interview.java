package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试记录实体 — 映射 interview 表
 * 每次模拟面试创建一条记录，记录面试配置、状态和最终评分
 */
@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID（当前固定为1） */
    private Long userId;

    /** 面试方向：java_backend / ai_dev / fullstack */
    private String direction;

    /** 难度等级：junior / mid / senior */
    private String difficulty;

    /** 面试类型：knowledge(知识点深挖) / project(项目经验) / comprehensive(综合) */
    private String interviewType;

    /** 面试状态：in_progress(进行中) / completed(已完成) */
    private String status;

    /** AI评估的总评分（1-10分，面试结束后填充） */
    private BigDecimal totalScore;

    /** 面试总时长（秒），从开始到结束 */
    private Integer durationSeconds;

    /** AI累计提问数 */
    private Integer questionCount;

    /** 面试开始时间 */
    private LocalDateTime createTime;

    /** 最近状态变更时间 */
    private LocalDateTime updateTime;
}
