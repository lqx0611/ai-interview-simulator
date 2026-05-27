package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 知识点评分实体 — 映射 topic_score 表
 * 面试报告中每个被考察知识点的独立评分记录
 */
@Data
@TableName("topic_score")
public class TopicScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的面试报告ID */
    private Long reportId;

    /** 知识点名称（如"JVM内存模型"、"Redis缓存策略"） */
    private String topic;

    /** AI给出的知识点评分（1-10分） */
    private BigDecimal score;

    /** AI对该知识点的评语 */
    private String comment;

    /** 是否为薄弱项：1=薄弱(score<6), 0=非薄弱 */
    private Integer isWeak;
}
