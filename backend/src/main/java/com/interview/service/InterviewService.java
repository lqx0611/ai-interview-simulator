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

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile(
            "\\{[^{}]*\"content\"[^{}]*\"action\"[^{}]*\\}", Pattern.DOTALL);

    public StartInterviewResponse startInterview(StartInterviewRequest request) {
        String systemPrompt = loadPromptTemplate("prompts/interviewer.st",
                request.getDirection(), request.getDifficulty(), request.getInterviewType());

        String aiText = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user("请开始面试，给出开场白和第一个问题。")
                .call()
                .content();

        log.info("AI response: {}", aiText);

        InterviewerResponse parsed = parseInterviewerResponse(aiText);

        Interview interview = new Interview();
        interview.setUserId(1L);
        interview.setDirection(request.getDirection());
        interview.setDifficulty(request.getDifficulty());
        interview.setInterviewType(request.getInterviewType());
        interview.setStatus("in_progress");
        interview.setQuestionCount(1);
        interviewMapper.insert(interview);

        Message message = new Message();
        message.setInterviewId(interview.getId());
        message.setRole("interviewer");
        message.setContent(parsed.getContent());
        message.setTopic(parsed.getCurrentTopic());
        messageMapper.insert(message);

        return new StartInterviewResponse(interview.getId(), parsed.getContent());
    }

    public void handleAnswer(Long interviewId, String userAnswer, SseEmitter emitter) {
        try {
            Interview interview = interviewMapper.selectById(interviewId);
            if (interview == null || !"in_progress".equals(interview.getStatus())) {
                sendErrorAndComplete(emitter, "面试不存在或已结束");
                return;
            }

            Message candidateMsg = new Message();
            candidateMsg.setInterviewId(interviewId);
            candidateMsg.setRole("candidate");
            candidateMsg.setContent(userAnswer);
            messageMapper.insert(candidateMsg);

            String systemPrompt = loadPromptTemplate("prompts/interviewer.st",
                    interview.getDirection(), interview.getDifficulty(), interview.getInterviewType());

            String context = buildConversationContext(interviewId);
            String userPrompt = context + "\n请根据候选人的最新回答，以面试官身份给出你的下一句回应（追问/引导/切换知识点/结束面试）。";

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
                        InterviewerResponse parsed = parseInterviewerResponse(aiText);

                        String content = parsed.getContent();
                        streamText(emitter, content);

                        if (parsed.getAnswerScore() != null) {
                            candidateMsg.setScore(BigDecimal.valueOf(parsed.getAnswerScore()));
                            candidateMsg.setTopic(parsed.getCurrentTopic());
                            messageMapper.updateById(candidateMsg);
                        }

                        Message interviewerMsg = new Message();
                        interviewerMsg.setInterviewId(interviewId);
                        interviewerMsg.setRole("interviewer");
                        interviewerMsg.setContent(content);
                        interviewerMsg.setTopic(parsed.getCurrentTopic());
                        messageMapper.insert(interviewerMsg);

                        interview.setQuestionCount(interview.getQuestionCount() + 1);
                        interviewMapper.updateById(interview);

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

    public EndInterviewResponse endInterview(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("面试不存在");
        }
        if ("completed".equals(interview.getStatus())) {
            throw new IllegalArgumentException("面试已结束");
        }

        List<Message> messages = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .eq("interview_id", interviewId)
                        .orderByAsc("create_time"));

        String conversationText = formatConversation(messages);

        String reportPrompt = loadReportTemplate(conversationText);

        log.info("Generating report for interview {}", interviewId);
        String aiText = chatClientBuilder.build()
                .prompt()
                .system("你是一位资深技术面试评估专家。请严格按照JSON格式输出评估报告。")
                .user(reportPrompt)
                .call()
                .content();

        log.info("Report AI response: {}", aiText);

        ReportResponse reportResponse = parseReportResponse(aiText);

        InterviewReport report = new InterviewReport();
        report.setInterviewId(interviewId);
        report.setOverallScore(reportResponse.getOverallScore() != null
                ? BigDecimal.valueOf(reportResponse.getOverallScore()) : BigDecimal.ZERO);
        report.setSummary(reportResponse.getSummary() != null ? reportResponse.getSummary() : "");
        report.setImprovement(reportResponse.getImprovement() != null ? reportResponse.getImprovement() : "");
        interviewReportMapper.insert(report);

        List<EndInterviewResponse.TopicScoreItem> topicItems = new ArrayList<>();
        if (reportResponse.getTopicScores() != null) {
            for (ReportResponse.TopicScoreItem item : reportResponse.getTopicScores()) {
                TopicScore ts = new TopicScore();
                ts.setReportId(report.getId());
                ts.setTopic(item.getTopic());
                ts.setScore(item.getScore() != null ? BigDecimal.valueOf(item.getScore()) : BigDecimal.ZERO);
                ts.setComment(item.getComment());
                ts.setIsWeak(item.isWeak() ? 1 : 0);
                topicScoreMapper.insert(ts);

                topicItems.add(new EndInterviewResponse.TopicScoreItem(
                        item.getTopic(),
                        ts.getScore(),
                        item.getComment(),
                        item.isWeak()));
            }
        }

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

    private String formatConversation(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            String roleLabel = "interviewer".equals(msg.getRole()) ? "面试官" : "候选人";
            sb.append(roleLabel).append("：").append(msg.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    private String loadReportTemplate(String conversationText) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/report.st");
            String template = resource.getContentAsString(StandardCharsets.UTF_8);
            return template.replace("{messages}", conversationText);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load report template", e);
        }
    }

    ReportResponse parseReportResponse(String aiText) {
        String json = extractJsonReport(aiText);
        try {
            return objectMapper.readValue(json, ReportResponse.class);
        } catch (Exception e) {
            log.warn("Report JSON parse failed, using fallback. Raw: {}", aiText);
        }
        return fallbackParseReport(aiText);
    }

    private String extractJsonReport(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

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

    private String buildConversationContext(Long interviewId) {
        List<Message> messages = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .eq("interview_id", interviewId)
                        .orderByDesc("create_time")
                        .last("LIMIT 20"));

        Collections.reverse(messages);

        StringBuilder sb = new StringBuilder("以下是面试对话记录：\n\n");
        for (Message msg : messages) {
            String roleLabel = "interviewer".equals(msg.getRole()) ? "面试官" : "候选人";
            sb.append(roleLabel).append("：").append(msg.getContent()).append("\n\n");
        }
        return sb.toString();
    }

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

    InterviewerResponse parseInterviewerResponse(String aiText) {
        String json = extractJson(aiText);
        try {
            return objectMapper.readValue(json, InterviewerResponse.class);
        } catch (Exception e) {
            log.warn("AI JSON parse failed, using fallback. Raw: {}", aiText);
        }
        return fallbackParse(aiText);
    }

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

    private String extractStringField(String text, String fieldName, String defaultValue) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return defaultValue;
    }

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

    private String switchDirectionLabel(String direction) {
        return switch (direction) {
            case "java_backend" -> "Java后端";
            case "ai_dev" -> "AI开发";
            case "fullstack" -> "全栈开发";
            default -> direction;
        };
    }

    private String switchDifficultyLabel(String difficulty) {
        return switch (difficulty) {
            case "junior" -> "初级";
            case "mid" -> "中级";
            case "senior" -> "高级";
            default -> difficulty;
        };
    }

    private String switchTypeLabel(String type) {
        return switch (type) {
            case "knowledge" -> "知识点深挖";
            case "project" -> "项目经验追问";
            case "comprehensive" -> "综合面试";
            default -> type;
        };
    }

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

    public InterviewDetailResponse getInterviewDetail(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("面试不存在");
        }

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
