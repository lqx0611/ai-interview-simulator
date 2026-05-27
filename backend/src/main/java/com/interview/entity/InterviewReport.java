package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试报告实体 — 映射 interview_report 表
 * 面试结束后AI生成的评估报告，与 interview 一对一关联
 */
@Data
@TableName("interview_report")
public class InterviewReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的面试记录ID（唯一，一次面试只生成一份报告） */
    private Long interviewId;

    /** AI综合评分（1-10分） */
    private BigDecimal overallScore;

    /** AI生成的面试总结评语 */
    private String summary;

    /** AI生成的改进建议（针对薄弱知识点的具体学习建议） */
    private String improvement;

    /** 报告生成时间 */
    private LocalDateTime createTime;
}
