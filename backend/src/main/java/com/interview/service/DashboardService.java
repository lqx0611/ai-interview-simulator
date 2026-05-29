package com.interview.service;

import com.interview.common.SecurityUtils;
import com.interview.dto.DashboardStatsResponse;
import com.interview.mapper.InterviewMapper;
import com.interview.mapper.TopicScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计看板服务
 * 聚合用户所有已完成面试的数据，计算知识点掌握度统计和薄弱项
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InterviewMapper interviewMapper;
    private final TopicScoreMapper topicScoreMapper;

    /**
     * 获取用户的知识点掌握度统计
     * 查询历史面试总数和总时长，按知识点聚合评分的平均/最高/最低及练习次数，
     * 计算掌握度等级（精通/熟练/了解/薄弱），输出薄弱知识点列表
     *
     * @return 统计数据：总面试次数、总时长、各知识点统计、薄弱知识点列表
     */
    public DashboardStatsResponse getStats() {
        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> stats = interviewMapper.selectStatsByUserId(userId);
        int totalInterviews = ((Number) stats.getOrDefault("total", 0)).intValue();
        long totalDuration = ((Number) stats.getOrDefault("duration", 0)).longValue();

        List<Map<String, Object>> rows = topicScoreMapper.selectTopicStatsByUserId(userId);

        List<DashboardStatsResponse.TopicStat> topicStats = new ArrayList<>();
        List<String> weakTopics = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String topic = (String) row.get("topic");
            BigDecimal avgScore = toBigDecimal(row.get("avg_score"), 2);
            BigDecimal maxScore = toBigDecimal(row.get("max_score"), 1);
            BigDecimal minScore = toBigDecimal(row.get("min_score"), 1);
            int practiceCount = ((Number) row.getOrDefault("practice_count", 0)).intValue();

            // 根据平均分划分掌握度等级
            String level = calcLevel(avgScore.doubleValue());

            LocalDateTime lastPracticeTime = null;
            Object timeObj = row.get("last_practice_time");
            if (timeObj instanceof Timestamp ts) {
                lastPracticeTime = ts.toLocalDateTime();
            }

            topicStats.add(new DashboardStatsResponse.TopicStat(
                    topic, avgScore, maxScore, minScore, practiceCount, level, lastPracticeTime));

            if ("weak".equals(level)) {
                weakTopics.add(topic);
            }
        }

        return new DashboardStatsResponse(totalInterviews, totalDuration, topicStats, weakTopics);
    }

    /** 根据平均分划分掌握度等级：>=8 精通, >=6 熟练, >=4 了解, <4 薄弱 */
    private String calcLevel(double avg) {
        if (avg >= 8) return "proficient";
        if (avg >= 6) return "skilled";
        if (avg >= 4) return "familiar";
        return "weak";
    }

    /** null安全的数值转BigDecimal，指定小数位数和四舍五入 */
    private BigDecimal toBigDecimal(Object val, int scale) {
        if (val == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(((Number) val).doubleValue())
                .setScale(scale, RoundingMode.HALF_UP);
    }
}
