package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.*;
import com.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 面试控制器
 * 处理面试全流程的HTTP请求：开始→对话→结束→历史/详情
 */
@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 开始面试
     * 根据用户选择的方向、难度和类型，调用AI生成开场白和第一个问题
     *
     * @param request 面试配置（方向、难度、类型）
     * @return 面试ID和AI生成的开场消息
     */
    @PostMapping("/api/interview/start")
    public Result<StartInterviewResponse> startInterview(@Valid @RequestBody StartInterviewRequest request) {
        StartInterviewResponse response = interviewService.startInterview(request);
        return Result.success(response);
    }

    /**
     * 提交回答并获取AI回复（SSE流式输出）
     * 用户提交答案后，AI分析回答质量并流式返回追问或下一个问题
     *
     * @param id 面试记录ID
     * @param request 用户回答内容
     * @return SseEmitter实例，AI回复通过SSE逐字推送到前端
     */
    @PostMapping(value = "/api/interview/{id}/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest request) {
        // 超时时间设为5分钟，避免长对话中SSE连接断开
        SseEmitter emitter = new SseEmitter(300_000L);
        interviewService.handleAnswer(id, request.getContent(), emitter);
        return emitter;
    }

    /**
     * 结束面试
     * 调用AI生成面试评估报告，包含总评分、知识点评分和改进建议
     *
     * @param id 面试记录ID
     * @return 报告ID、总评分、总结和各知识点评分明细
     */
    @PostMapping("/api/interview/{id}/end")
    public Result<EndInterviewResponse> endInterview(@PathVariable Long id) {
        EndInterviewResponse response = interviewService.endInterview(id);
        return Result.success(response);
    }

    /**
     * 练习历史分页列表
     * 返回当前用户已完成面试的列表，按时间倒序排列
     *
     * @param page 页码，从1开始，默认1
     * @param size 每页条数，范围1-50，超出范围自动修正为10
     * @return 分页结果，每条记录含方向、难度、评分、时长、题数、时间
     */
    @GetMapping("/api/interview/history")
    public Result<PageResponse<HistoryItemResponse>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 对前端传入的参数做边界保护，避免非法分页参数
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        PageResponse<HistoryItemResponse> response = interviewService.getHistory(page, size);
        return Result.success(response);
    }

    /**
     * 面试详情
     * 返回单次面试的完整信息：基本信息 + 对话消息列表 + 面试报告
     *
     * @param id 面试记录ID
     * @return 面试详情（含消息列表和报告）
     */
    @GetMapping("/api/interview/{id}/detail")
    public Result<InterviewDetailResponse> detail(@PathVariable Long id) {
        InterviewDetailResponse response = interviewService.getInterviewDetail(id);
        return Result.success(response);
    }
}
