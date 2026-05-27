package com.interview.controller;

import com.interview.common.Result;
import com.interview.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供探活接口，用于验证服务是否正常启动、数据库是否可连通
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final UserMapper userMapper;

    /**
     * 健康检查接口
     * 查询数据库记录数，返回服务状态和当前时间戳
     *
     * @return 包含message、timestamp、db_ok、user_count的状态信息
     */
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
