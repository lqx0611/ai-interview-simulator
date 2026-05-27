package com.interview.controller;

import com.interview.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI连通性测试控制器
 * 用于验证Spring AI与DeepSeek（或其他大模型）的连通性是否正常
 */
@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * AI对话测试接口
     * 发送自定义prompt到AI模型，返回模型回复内容
     *
     * @param prompt 测试提示词，默认为"你好，请用一句话介绍你自己"
     * @return AI模型的回复文本
     */
    @GetMapping("/api/ai/test")
    public Result<String> testChat(@RequestParam(defaultValue = "你好，请用一句话介绍你自己") String prompt) {
        String response = chatClientBuilder.build()
                .prompt(prompt)
                .call()
                .content();
        return Result.success(response);
    }
}
