package com.cqjtjc.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqjtjc.system.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserMapper 集成测试（H2 内存库 + schema-test.sql）
 */
@SpringBootTest
@ActiveProfiles("test")
class SysUserMapperTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void insertAndSelectOne_byUsername_returnsUser() {
        SysUser user = new SysUser();
        user.setUsername("mapper_test_user");
        user.setPassword("encoded");
        user.setNickname("测试用户");
        user.setStatus(1);
        int rows = sysUserMapper.insert(user);
        assertEquals(1, rows);
        assertNotNull(user.getId());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, "mapper_test_user");
        SysUser found = sysUserMapper.selectOne(wrapper);

        assertNotNull(found);
        assertEquals(user.getId(), found.getId());
        assertEquals("mapper_test_user", found.getUsername());
        assertEquals("测试用户", found.getNickname());
    }

    @Test
    void selectById_whenExists_returnsUser() {
        // schema-test 无初始数据，先插入
        SysUser user = new SysUser();
        user.setUsername("select_by_id_user");
        user.setPassword("pwd");
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysUser found = sysUserMapper.selectById(user.getId());
        assertNotNull(found);
        assertEquals("select_by_id_user", found.getUsername());
    }
}
