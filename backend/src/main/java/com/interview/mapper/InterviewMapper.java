package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.Interview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {

    @Select("SELECT COUNT(*) AS total, COALESCE(SUM(duration_seconds), 0) AS duration " +
            "FROM interview WHERE user_id = #{userId} AND status = 'completed'")
    Map<String, Object> selectStatsByUserId(@Param("userId") Long userId);
}
