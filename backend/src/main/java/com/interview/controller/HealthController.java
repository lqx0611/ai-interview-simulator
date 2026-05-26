package com.interview.controller;

import com.interview.common.Result;
import com.interview.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final UserMapper userMapper;

    @GetMapping("/api/hello")
    public Result<Map<String, Object>> hello() {
        long userCount = userMapper.selectCount(null);
        return Result.success(Map.of(
                "message", "hello",
                "timestamp", LocalDateTime.now().toString(),
                "db_ok", true,
                "user_count", userCount
        ));
    }
}
