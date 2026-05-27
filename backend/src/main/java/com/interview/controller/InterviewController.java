package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.*;
import com.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/api/interview/start")
    public Result<StartInterviewResponse> startInterview(@Valid @RequestBody StartInterviewRequest request) {
        StartInterviewResponse response = interviewService.startInterview(request);
        return Result.success(response);
    }

    @PostMapping(value = "/api/interview/{id}/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest request) {
        SseEmitter emitter = new SseEmitter(300_000L);
        interviewService.handleAnswer(id, request.getContent(), emitter);
        return emitter;
    }

    @PostMapping("/api/interview/{id}/end")
    public Result<EndInterviewResponse> endInterview(@PathVariable Long id) {
        EndInterviewResponse response = interviewService.endInterview(id);
        return Result.success(response);
    }

    @GetMapping("/api/interview/history")
    public Result<PageResponse<HistoryItemResponse>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        PageResponse<HistoryItemResponse> response = interviewService.getHistory(page, size);
        return Result.success(response);
    }

    @GetMapping("/api/interview/{id}/detail")
    public Result<InterviewDetailResponse> detail(@PathVariable Long id) {
        InterviewDetailResponse response = interviewService.getInterviewDetail(id);
        return Result.success(response);
    }
}
