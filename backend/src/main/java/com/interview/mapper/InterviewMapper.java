package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.Interview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 面试记录数据访问层
 * 扩展MyBatis-Plus BaseMapper，提供自定义聚合查询
 */
@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {

    /**
     * 查询用户面试统计（总次数、总时长）
     * 只统计已完成状态的面试记录
     *
     * @param userId 用户ID
     * @return Map包含 total（总面试次数）和 duration（总时长秒数）
     */
    @Select("SELECT COUNT(*) AS total, COALESCE(SUM(duration_seconds), 0) AS duration " +
            "FROM interview WHERE user_id = #{userId} AND status = 'completed'")
    Map<String, Object> selectStatsByUserId(@Param("userId") Long userId);
}
