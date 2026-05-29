package com.interview.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口调用频率限制注解
 * 标记在Controller方法上，配合RateLimitInterceptor实现Redis计数限流
 *
 * Step 4.6：同一用户在规定时间内调用不超过指定次数，超出返回429
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流最大次数，默认每分钟5次 */
    int maxCalls() default 5;

    /** 限流时间窗口（秒），默认60秒 */
    int windowSeconds() default 60;

    /** 限流提示信息 */
    String message() default "操作过于频繁，请稍后再试";
}
