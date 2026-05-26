package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("topic_score")
public class TopicScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;

    private String topic;

    private BigDecimal score;

    private String comment;

    private Integer isWeak;
}
