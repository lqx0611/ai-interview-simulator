package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.TopicScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TopicScoreMapper extends BaseMapper<TopicScore> {

    @Select("SELECT ts.topic, AVG(ts.score) AS avg_score, MAX(ts.score) AS max_score, " +
            "MIN(ts.score) AS min_score, COUNT(*) AS practice_count, " +
            "MAX(i.create_time) AS last_practice_time " +
            "FROM topic_score ts " +
            "JOIN interview_report ir ON ts.report_id = ir.id " +
            "JOIN interview i ON ir.interview_id = i.id " +
            "WHERE i.user_id = #{userId} " +
            "GROUP BY ts.topic")
    List<Map<String, Object>> selectTopicStatsByUserId(@Param("userId") Long userId);
}
