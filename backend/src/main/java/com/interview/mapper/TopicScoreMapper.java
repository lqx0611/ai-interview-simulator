package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.TopicScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 知识点评分数据访问层
 * 扩展MyBatis-Plus BaseMapper，提供知识点维度的聚合统计查询
 */
@Mapper
public interface TopicScoreMapper extends BaseMapper<TopicScore> {

    /**
     * 按知识点聚合评分统计
     * 三表JOIN（topic_score → interview_report → interview），
     * 按知识点分组计算平均分、最高分、最低分、练习次数、最近练习时间
     *
     * @param userId 用户ID
     * @return 每个知识点的聚合统计（avg_score, max_score, min_score, practice_count, last_practice_time）
     */
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
