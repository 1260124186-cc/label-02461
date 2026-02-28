package com.cqjtjc.common.result;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分页结果 PageResult 的单元测试
 */
class PageResultTest {

    @Test
    void constructor_fullArgs() {
        List<String> records = Arrays.asList("a", "b", "c");
        PageResult<String> result = new PageResult<>(10L, 1L, 10L, 1L, records);
        assertEquals(10L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
        assertEquals(1L, result.getPages());
        assertEquals(records, result.getRecords());
    }

    @Test
    void constructor_totalAndRecords() {
        List<Integer> records = Arrays.asList(1, 2, 3);
        PageResult<Integer> result = new PageResult<>(3L, records);
        assertEquals(3L, result.getTotal());
        assertEquals(records, result.getRecords());
        assertEquals(1L, result.getCurrent());
        assertEquals(3, result.getRecords().size());
        assertTrue(result.getPages() >= 1);
    }

    @Test
    void constructor_nullRecords_treatedAsEmpty() {
        PageResult<Object> result = new PageResult<>(0L, null);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
        assertEquals(0L, result.getPages());
    }

    @Test
    void constructor_emptyRecords() {
        PageResult<String> result = new PageResult<>(0L, Collections.emptyList());
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }
}
