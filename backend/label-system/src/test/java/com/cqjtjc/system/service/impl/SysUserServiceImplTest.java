package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.system.dto.SysUserDTO;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.mapper.SysUserMapper;
import com.cqjtjc.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysUserServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper baseMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl userService;

    private SysUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new SysUser();
        existingUser.setId(1L);
        existingUser.setUsername("admin");
        existingUser.setPassword("encoded");
        existingUser.setNickname("管理员");
        existingUser.setStatus(1);
    }

    @Test
    void getByUsername_whenUserExists_returnsUser() {
        when(baseMapper.selectOne(any())).thenReturn(existingUser);

        SysUser result = userService.getByUsername("admin");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(1L, result.getId());
    }

    @Test
    void getByUsername_whenUserNotExists_returnsNull() {
        when(baseMapper.selectOne(any())).thenReturn(null);

        SysUser result = userService.getByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    void addUser_whenUsernameExists_throwsBusinessException() {
        when(baseMapper.selectOne(any())).thenReturn(existingUser);

        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("admin");
        dto.setPassword("pass");
        dto.setNickname("测试");
        dto.setRoleIds(Collections.emptyList());

        assertThrows(BusinessException.class, () -> userService.addUser(dto));
        verify(baseMapper, never()).insert(any());
    }

    @Test
    void addUser_whenUsernameNotExists_savesAndReturnsId() {
        when(baseMapper.selectOne(any())).thenReturn(null);
        when(baseMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser u = inv.getArgument(0);
            u.setId(100L);
            return 1;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");

        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("newuser");
        dto.setPassword("pass123");
        dto.setNickname("新用户");
        dto.setRoleIds(Collections.emptyList());

        Long id = userService.addUser(dto);

        assertNotNull(id);
        assertEquals(100L, id);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(baseMapper).insert(userCaptor.capture());
        SysUser saved = userCaptor.getValue();
        assertEquals("newuser", saved.getUsername());
        assertEquals("encodedPass", saved.getPassword());
        assertEquals("新用户", saved.getNickname());
    }

    @Test
    void resetPassword_whenUserNotExists_throwsBusinessException() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.resetPassword(999L, "newPass"));
        verify(baseMapper, never()).updateById(any());
    }

    @Test
    void resetPassword_whenUserExists_updatesPassword() {
        when(baseMapper.selectById(1L)).thenReturn(existingUser);
        when(baseMapper.updateById(any(SysUser.class))).thenReturn(1);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        userService.resetPassword(1L, "newPass");

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(baseMapper).updateById(userCaptor.capture());
        assertEquals("encodedNew", userCaptor.getValue().getPassword());
    }
}
