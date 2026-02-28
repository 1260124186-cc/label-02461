package com.cqjtjc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 启动类上下文加载测试（使用 H2 内存库，无需 MySQL）
 */
@SpringBootTest
@ActiveProfiles("test")
class LabelApplicationTests {

    @Test
    void contextLoads() {
    }
}
