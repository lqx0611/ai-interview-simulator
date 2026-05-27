package com.interview.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dto.EndInterviewResponse;
import com.interview.dto.HistoryItemResponse;
import com.interview.dto.InterviewDetailResponse;
import com.interview.dto.InterviewerResponse;
import com.interview.dto.PageResponse;
import com.interview.dto.ReportResponse;
import com.interview.dto.StartInterviewRequest;
import com.interview.dto.StartInterviewResponse;
import com.interview.entity.Interview;
import com.interview.entity.InterviewReport;
import com.interview.entity.Message;
import com.interview.entity.TopicScore;
import com.interview.mapper.InterviewMapper;
import com.interview.mapper.InterviewReportMapper;
import com.interview.mapper.MessageMapper;
import com.interview.mapper.TopicScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 面试服务 — 处理面试核心业务逻辑
 * 包括面试的创建与启动、AI对话流式交互、面试结束报告生成、历史记录查询等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final ChatClient.Builder chatClientBuilder;
    private final InterviewMapper interviewMapper;
    private final MessageMapper messageMapper;
    private final InterviewReportMapper interviewReportMapper;
    private final TopicScoreMapper topicScoreMapper;
    private final ObjectMapper objectMapper;

    /** 用于从AI非标准JSON响应中提取合法JSON的正则 — 匹配包含content和action字段的JSON对象 */
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
            "\\{[^{}]*\"content\"[^{}]*\"action\"[^{}]*\\}", Pattern.DOTALL);

    /**
     * 开始面试
     * 加载面试官角色Prompt模板，调用AI生成开场白，创建面试记录并返回
     *
     * @param request 面试配置（方向、难度、类型）
     * @return 面试ID和AI生成的第一个问题
     */
    public StartInterviewResponse startInterview(StartInterviewRequest request) {
        // 加载Prompt模板，将方向/难度/类型占位符替换为中文标签
        String systemPrompt = loadPromptTemplate("prompts/interviewer.st",
                request.getDirection(), request.getDifficulty(), request.getInterviewType());

        String aiText = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user("请开始面试，给出开场白和第一个问题。")
                .call()
                .content();

        log.info("AI response: {}", aiText);

        // AI返回可能是非标准JSON，需要容错解析
        InterviewerResponse parsed = parseInterviewerResponse(aiText);

        // 创建面试记录，状态设为进行中
        Interview interview = new Interview();
        interview.setUserId(1L);
        interview.setDirection(request.getDirection());
        interview.setDifficulty(request.getDifficulty());
        interview.setInterviewType(request.getInterviewType());
        interview.setStatus("in_progress");
        interview.setQuestionCount(1);
        interviewMapper.insert(interview);

        // 保存AI的开场消息
        Message message = new Message();
        message.setInterviewId(interview.getId());
        message.setRole("interviewer");
        message.setContent(parsed.getContent());
        message.setTopic(parsed.getCurrentTopic());
        messageMapper.insert(message);

        return new StartInterviewResponse(interview.getId(), parsed.getContent());
    }

    /**
     * 处理用户回答，流式返回AI追问/回复
     * 核心对话流程：保存用户消息 → 构建上下文 → 调用AI流式输出 → 逐字推送给前端
     *
     * @param interviewId 面试记录ID
     * @param userAnswer  用户回答文本
     * @param emitter     SSE连接，用于向前端推送事件
     */
    public void handleAnswer(Long interviewId, String userAnswer, SseEmitter emitter) {
        try {
            Interview interview = interviewMapper.selectById(interviewId);
            if (interview == null || !"in_progress".equals(interview.getStatus())) {
                sendErrorAndComplete(emitter, "面试不存在或已结束");
                return;
            }

            // 1. 保存用户回答
            Message candidateMsg = new Message();
            candidateMsg.setInterviewId(interviewId);
            candidateMsg.setRole("candidate");
            candidateMsg.setContent(userAnswer);
            messageMapper.insert(candidateMsg);

            // 2. 构建AI调用上下文（含系统Prompt + 最近对话记录）
            String systemPrompt = loadPromptTemplate("prompts/interviewer.st",
                    interview.getDirection(), interview.getDifficulty(), interview.getInterviewType());

            String context = buildConversationContext(interviewId);
            String userPrompt = context + "\n请根据候选人的最新回答，以面试官身份给出你的下一句回应（追问/引导/切换知识点/结束面试）。";

            // 3. 流式调用AI，收集完整回复后逐字推送
            ChatClient chatClient = chatClientBuilder.build();
            StringBuilder fullResponse = new StringBuilder();

            chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .stream()
                    .content()
                    .doOnNext(fullResponse::append)
                    .doOnComplete(() -> {
                        String aiText = fullResponse.toString();
                        log.info("AI stream complete, full response: {}", aiText);

                        // 容错解析AI返回的JSON
                        InterviewerResponse parsed = parseInterviewerResponse(aiText);

                        String content = parsed.getContent();
                        // 逐字推送，实现打字机效果（每字间隔30ms）
                        streamText(emitter, content);

                        // 回填AI对用户回答的评分
                        if (parsed.getAnswerScore() != null) {
                            candidateMsg.setScore(BigDecimal.valueOf(parsed.getAnswerScore()));
                            candidateMsg.setTopic(parsed.getCurrentTopic());
                            messageMapper.updateById(candidateMsg);
                        }

                        // 保存AI消息
                        Message interviewerMsg = new Message();
                        interviewerMsg.setInterviewId(interviewId);
                        interviewerMsg.setRole("interviewer");
                        interviewerMsg.setContent(content);
                        interviewerMsg.setTopic(parsed.getCurrentTopic());
                        messageMapper.insert(interviewerMsg);

                        // 更新面试提问计数
                        interview.setQuestionCount(interview.getQuestionCount() + 1);
                        interviewMapper.updateById(interview);

                        // 推送done事件，告知前端本轮对话完成
                        Map<String, Object> doneData = new LinkedHashMap<>();
                        doneData.put("topic", parsed.getCurrentTopic() != null ? parsed.getCurrentTopic() : "");
                        doneData.put("score", parsed.getAnswerScore());
                        doneData.put("action", parsed.getAction() != null ? parsed.getAction() : "next");
                        sendEvent(emitter, "done", doneData);

                        emitter.complete();
                    })
                    .doOnError(e -> {
                        log.error("AI stream error", e);
                        sendErrorAndComplete(emitter, "AI响应出错: " + e.getMessage());
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("Answer handling error", e);
            sendErrorAndComplete(emitter, "处理回答时出错");
        }
    }

    /**
     * 结束面试并生成评估报告
     * 加载完整对话记录，调用AI生成报告JSON，解析后持久化报告和知识点评分
     *
     * @param interviewId 面试记录ID
     * @return 报告ID、总评分、总结、知识点评分明细、改进建议
     */
    public EndInterviewResponse endInterview(Long interviewId) {
        // 1. 校验面试状态
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("面试不存在");
        }
        if ("completed".equals(interview.getStatus())) {
            throw new IllegalArgumentException("面试已结束");
        }

        // 2. 加载完整对话记录
        List<Message> messages = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .eq("interview_id", interviewId)
                        .orderByAsc("create_time"));

        String conversationText = formatConversation(messages);

        // 3. 加载报告生成Prompt模板，替换对话内容占位符
        String reportPrompt = loadReportTemplate(conversationText);

        // 4. 调用AI生成报告
        log.info("Generating report for interview {}", interviewId);
        String aiText = chatClientBuilder.build()
                .prompt()
                .system("你是一位资深技术面试评估专家。请严格按照JSON格式输出评估报告。")
                .user(reportPrompt)
                .call()
                .content();

        log.info("Report AI response: {}", aiText);

        // 5. 解析报告JSON并持久化
        ReportResponse reportResponse = parseReportResponse(aiText);

        InterviewReport report = new InterviewReport();
        report.setInterviewId(interviewId);
        report.setOverallScore(reportResponse.getOverallScore() != null
                ? BigDecimal.valueOf(reportResponse.getOverallScore()) : BigDecimal.ZERO);
        report.setSummary(reportResponse.getSummary() != null ? reportResponse.getSummary() : "");
        report.setImprovement(reportResponse.getImprovement() != null ? reportResponse.getImprovement() : "");
        interviewReportMapper.insert(report);

        // 6. 逐条保存知识点评分
        List<EndInterviewResponse.TopicScoreItem> topicItems = new ArrayList<>();
        if (reportResponse.getTopicScores() != null) {
            for (ReportResponse.TopicScoreItem item : reportResponse.getTopicScores()) {
                TopicScore ts = new TopicScore();
                ts.setReportId(report.getId());
                ts.setTopic(item.getTopic());
                ts.setScore(item.getScore() != null ? BigDecimal.valueOf(item.getScore()) : BigDecimal.ZERO);
                ts.setComment(item.getComment());
                // isWeak按规则生成: 分值<6为薄弱项
                ts.setIsWeak(item.isWeak() ? 1 : 0);
                topicScoreMapper.insert(ts);

                topicItems.add(new EndInterviewResponse.TopicScoreItem(
                        item.getTopic(),
                        ts.getScore(),
                        item.getComment(),
                        item.isWeak()));
            }
        }

        // 7. 更新面试状态为已完成，记录时长
        long durationSeconds = java.time.Duration.between(
                interview.getCreateTime(), java.time.LocalDateTime.now()).getSeconds();
        interview.setStatus("completed");
        interview.setTotalScore(report.getOverallScore());
        interview.setDurationSeconds((int) durationSeconds);
        interviewMapper.updateById(interview);

        return new EndInterviewResponse(
                report.getId(),
                report.getOverallScore(),
                report.getSummary(),
                topicItems,
                report.getImprovement(),
                interview.getDurationSeconds() != null ? interview.getDurationSeconds() : 0,
                interview.getQuestionCount() != null ? interview.getQuestionCount() : 0);
    }

    /**
     * 将消息列表格式化为"面试官：... / 候选人：..."格式的对话文本
     * 用于作为AI报告生成或对话续写的输入
     */
    private String formatConversation(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            String roleLabel = "interviewer".equals(msg.getRole()) ? "面试官" : "候选人";
            sb.append(roleLabel).append("：").append(msg.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    /** 加载报告生成Prompt模板，将对话记录占位符替换为实际内容 */
    private String loadReportTemplate(String conversationText) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/report.st");
            String template = resource.getContentAsString(StandardCharsets.UTF_8);
            return template.replace("{messages}", conversationText);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load report template", e);
        }
    }

    /**
     * 解析AI返回的报告JSON
     * 先尝试标准JSON解析，失败则使用正则fallback提取关键字段
     */
    ReportResponse parseReportResponse(String aiText) {
        String json = extractJsonReport(aiText);
        try {
            return objectMapper.readValue(json, ReportResponse.class);
        } catch (Exception e) {
            log.warn("Report JSON parse failed, using fallback. Raw: {}", aiText);
        }
        return fallbackParseReport(aiText);
    }

    /** 从AI文本中提取JSON对象，取第一个{到最后一个}之间的内容 */
    private String extractJsonReport(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /** 正则Fallback：从非标准JSON文本中提取报告关键字段（整体评分、总结、改进建议） */
    private ReportResponse fallbackParseReport(String text) {
        ReportResponse resp = new ReportResponse();
        String scoreStr = extractStringField(text, "overall_score", "5");
        try {
            resp.setOverallScore(Integer.parseInt(scoreStr.replaceAll("[^0-9]", "")));
        } catch (NumberFormatException e) {
            resp.setOverallScore(5);
        }
        resp.setSummary(extractStringField(text, "summary", "面试完成"));
        resp.setImprovement(extractStringField(text, "improvement", "请参考对话记录"));
        return resp;
    }

    /**
     * 构建AI对话上下文（最近20条消息）
     * 只取最近20条避免超出大模型Token限制，读取后反转回时间顺序
     */
    private String buildConversationContext(Long interviewId) {
        List<Message> messages = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .eq("interview_id", interviewId)
                        .orderByDesc("create_time")
                        .last("LIMIT 20"));

        // 按create_time降序查出来后反转，恢复为正序的对话顺序
        Collections.reverse(messages);

        StringBuilder sb = new StringBuilder("以下是面试对话记录：\n\n");
        for (Message msg : messages) {
            String roleLabel = "interviewer".equals(msg.getRole()) ? "面试官" : "候选人";
            sb.append(roleLabel).append("：").append(msg.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 逐字推送文本（打字机效果）
     * 每个字符间隔30ms，模拟真人打字的阅读节奏
     */
    private void streamText(SseEmitter emitter, String text) {
        for (int i = 0; i < text.length(); i++) {
            String chunk = text.substring(i, i + 1);
            sendEvent(emitter, "content", Map.of("text", chunk));
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /** 通过SSE向客户端发送带type标签的JSON事件 */
    private void sendEvent(SseEmitter emitter, String type, Map<String, Object> data) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", type);
            event.putAll(data);
            emitter.send(SseEmitter.event()
                    .data(objectMapper.writeValueAsString(event)));
        } catch (IOException e) {
            log.error("Failed to send SSE event", e);
        }
    }

    /** 发送错误事件并关闭SSE连接（以异常状态结束） */
    private void sendErrorAndComplete(SseEmitter emitter, String errorMsg) {
        try {
            Map<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("type", "error");
            errorData.put("message", errorMsg);
            emitter.send(SseEmitter.event()
                    .data(objectMapper.writeValueAsString(errorData)));
        } catch (IOException ignored) {
        }
        emitter.completeWithError(new RuntimeException(errorMsg));
    }

    /**
     * 解析面试官角色的AI回复JSON
     * 先用正则匹配含content+action字段的JSON对象，再用Jackson反序列化；失败则正则fallback
     */
    InterviewerResponse parseInterviewerResponse(String aiText) {
        String json = extractJson(aiText);
        try {
            return objectMapper.readValue(json, InterviewerResponse.class);
        } catch (Exception e) {
            log.warn("AI JSON parse failed, using fallback. Raw: {}", aiText);
        }
        return fallbackParse(aiText);
    }

    /**
     * 从AI原始响应中提取JSON
     * 优先使用正则匹配包含content和action字段的JSON块（兼容AI返回多余文本的情况）
     * 匹配不到时退回到取第一个{到最后一个}之间的内容
     */
    private String extractJson(String text) {
        java.util.regex.Matcher m = JSON_BLOCK_PATTERN.matcher(text);
        if (m.find()) {
            return m.group();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /** 正则Fallback：逐个提取content、current_topic、action、answer_score字段 */
    private InterviewerResponse fallbackParse(String text) {
        InterviewerResponse resp = new InterviewerResponse();
        resp.setContent(extractStringField(text, "content", text));
        resp.setCurrentTopic(extractStringField(text, "current_topic", "综合面试"));
        resp.setAction(extractStringField(text, "action", "next"));
        String scoreStr = extractStringField(text, "answer_score", null);
        if (scoreStr != null && !"null".equals(scoreStr)) {
            try {
                resp.setAnswerScore(Integer.parseInt(scoreStr.replaceAll("[^0-9]", "")));
            } catch (NumberFormatException ignored) {
            }
        }
        return resp;
    }

    /** 正则提取JSON字段值：匹配 "fieldName":"value" 模式，返回value部分 */
    private String extractStringField(String text, String fieldName, String defaultValue) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return defaultValue;
    }

    /**
     * 加载Prompt模板文件，替换方向/难度/类型的占位符
     * 将英文枚举值映射为中文标签后填入{@code {direction}}、{@code {difficulty}}、{@code {interview_type}}
     */
    private String loadPromptTemplate(String path, String direction, String difficulty, String interviewType) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String template = resource.getContentAsString(StandardCharsets.UTF_8);
            String directionLabel = switchDirectionLabel(direction);
            String difficultyLabel = switchDifficultyLabel(difficulty);
            String typeLabel = switchTypeLabel(interviewType);
            return template
                    .replace("{direction}", directionLabel)
                    .replace("{difficulty}", difficultyLabel)
                    .replace("{interview_type}", typeLabel);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template: " + path, e);
        }
    }

    /** 将面试方向枚举值转换为中文标签 */
    private String switchDirectionLabel(String direction) {
        return switch (direction) {
            case "java_backend" -> "Java后端";
            case "ai_dev" -> "AI开发";
            case "fullstack" -> "全栈开发";
            default -> direction;
        };
    }

    /** 将难度枚举值转换为中文标签 */
    private String switchDifficultyLabel(String difficulty) {
        return switch (difficulty) {
            case "junior" -> "初级";
            case "mid" -> "中级";
            case "senior" -> "高级";
            default -> difficulty;
        };
    }

    /** 将面试类型枚举值转换为中文标签 */
    private String switchTypeLabel(String type) {
        return switch (type) {
            case "knowledge" -> "知识点深挖";
            case "project" -> "项目经验追问";
            case "comprehensive" -> "综合面试";
            default -> type;
        };
    }

    /**
     * 分页查询历史面试记录
     * 只返回当前用户（user_id=1）已完成状态的面试，按创建时间倒序
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果包含HistoryItemResponse列表
     */
    public PageResponse<HistoryItemResponse> getHistory(int page, int size) {
        Page<Interview> pageParam = new Page<>(page, size);
        QueryWrapper<Interview> wrapper = new QueryWrapper<Interview>()
                .eq("user_id", 1L)
                .eq("status", "completed")
                .orderByDesc("create_time");

        Page<Interview> result = interviewMapper.selectPage(pageParam, wrapper);

        List<HistoryItemResponse> list = result.getRecords().stream()
                .map(i -> new HistoryItemResponse(
                        i.getId(),
                        i.getDirection(),
                        i.getDifficulty(),
                        i.getInterviewType(),
                        i.getTotalScore(),
                        i.getQuestionCount(),
                        i.getDurationSeconds(),
                        i.getCreateTime()))
                .collect(Collectors.toList());

        return new PageResponse<>(list, result.getTotal(), page, size);
    }

    /**
     * 查询面试详情（完整对话 + 报告 + 知识点评分）
     *
     * @param interviewId 面试记录ID
     * @return 包含基本信息、消息列表和报告详情的完整面试数据
     * @throws IllegalArgumentException 面试不存在时抛出
     */
    public InterviewDetailResponse getInterviewDetail(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("面试不存在");
        }

        // 查询所有消息，按时间正序排列
        List<Message> messages = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .eq("interview_id", interviewId)
                        .orderByAsc("create_time"));

        List<InterviewDetailResponse.MessageItem> messageItems = messages.stream()
                .map(m -> new InterviewDetailResponse.MessageItem(
                        m.getRole(),
                        m.getContent(),
                        m.getTopic(),
                        m.getScore(),
                        m.getCreateTime()))
                .collect(Collectors.toList());

        // 查询报告（一对一关系），有则组装报告信息（含知识点评分）
        InterviewReport report = interviewReportMapper.selectOne(
                new QueryWrapper<InterviewReport>().eq("interview_id", interviewId));

        InterviewDetailResponse.ReportInfo reportInfo = null;
        if (report != null) {
            List<TopicScore> topicScores = topicScoreMapper.selectList(
                    new QueryWrapper<TopicScore>().eq("report_id", report.getId()));

            List<InterviewDetailResponse.TopicScoreItem> topicItems = topicScores.stream()
                    .map(t -> new InterviewDetailResponse.TopicScoreItem(
                            t.getTopic(),
                            t.getScore(),
                            t.getComment(),
                            t.getIsWeak() != null && t.getIsWeak() == 1))
                    .collect(Collectors.toList());

            reportInfo = new InterviewDetailResponse.ReportInfo(
                    report.getId(),
                    report.getOverallScore(),
                    report.getSummary(),
                    report.getImprovement(),
                    topicItems);
        }

        return new InterviewDetailResponse(
                interview.getId(),
                interview.getDirection(),
                interview.getDifficulty(),
                interview.getInterviewType(),
                interview.getTotalScore(),
                interview.getQuestionCount(),
                interview.getDurationSeconds(),
                interview.getCreateTime(),
                messageItems,
                reportInfo);
    }
}
