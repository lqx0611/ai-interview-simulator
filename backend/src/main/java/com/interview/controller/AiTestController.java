package com.interview.controller;

import com.interview.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final ChatClient.Builder chatClientBuilder;

    @GetMapping("/api/ai/test")
    public Result<String> testChat(@RequestParam(defaultValue = "你好，请用一句话介绍你自己") String prompt) {
        String response = chatClientBuilder.build()
                .prompt(prompt)
                .call()
                .content();
        return Result.success(response);
    }
}
