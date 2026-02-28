package com.cqjtjc.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一响应 R 的单元测试
 */
class RTest {

    @Test
    void ok_withoutData() {
        R<Object> r = R.ok();
        assertEquals(200, r.getCode());
        assertEquals("操作成功", r.getMsg());
        assertNull(r.getData());
    }

    @Test
    void ok_withData() {
        R<String> r = R.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("操作成功", r.getMsg());
        assertEquals("hello", r.getData());
    }

    @Test
    void ok_withDataAndMsg() {
        R<Integer> r = R.ok(100, "查询成功");
        assertEquals(200, r.getCode());
        assertEquals("查询成功", r.getMsg());
        assertEquals(100, r.getData());
    }

    @Test
    void fail_default() {
        R<Object> r = R.fail();
        assertEquals(500, r.getCode());
        assertEquals("操作失败", r.getMsg());
        assertNull(r.getData());
    }

    @Test
    void fail_withMsg() {
        R<Object> r = R.fail("用户名已存在");
        assertEquals(500, r.getCode());
        assertEquals("用户名已存在", r.getMsg());
        assertNull(r.getData());
    }

    @Test
    void fail_withCodeAndMsg() {
        R<Object> r = R.fail(401, "未登录");
        assertEquals(401, r.getCode());
        assertEquals("未登录", r.getMsg());
        assertNull(r.getData());
    }
}
