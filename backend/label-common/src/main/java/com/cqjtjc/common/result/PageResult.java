package com.cqjtjc.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果（用于 API 返回，避免直接序列化 MyBatis-Plus Page 导致 Jackson 异常）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private long total;
    private long current;
    private long size;
    private long pages;
    private List<T> records;

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records != null ? records : Collections.emptyList();
        this.current = 1;
        this.size = this.records.size();
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }
}
