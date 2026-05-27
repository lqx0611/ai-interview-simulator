package com.interview.common;

import lombok.Data;

/**
 * 统一响应包装类
 * 所有API接口均返回 Result<T> 结构，便于前端统一处理
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {

    /** 业务状态码：200=成功，其他=失败 */
    private int code;
    /** 状态消息 */
    private String message;
    /** 响应数据载荷 */
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功响应（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功响应（无数据，如删除操作） */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /** 失败响应（自定义错误码） */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败响应（默认500状态码） */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}
