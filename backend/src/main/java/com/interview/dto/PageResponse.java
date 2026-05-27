package com.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 通用分页响应体
 *
 * @param <T> 列表元素类型
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    /** 当前页数据列表 */
    private List<T> list;
    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private int page;
    /** 每页条数 */
    private int size;
}
