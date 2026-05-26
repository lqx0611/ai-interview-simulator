package com.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String direction;

    private String difficulty;

    private String interviewType;

    private String status;

    private BigDecimal totalScore;

    private Integer durationSeconds;

    private Integer questionCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
