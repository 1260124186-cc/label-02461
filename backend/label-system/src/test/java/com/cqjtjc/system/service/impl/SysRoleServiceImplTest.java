package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.system.dto.SysRoleDTO;
import com.cqjtjc.system.entity.SysRole;
import com.cqjtjc.system.entity.SysRoleMenu;
import com.cqjtjc.system.mapper.SysRoleMapper;
import com.cqjtjc.system.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysRoleServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper baseMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @InjectMocks
    private SysRoleServiceImpl roleService;

    private SysRole existingRole;

    @BeforeEach
    void setUp() {
        existingRole = new SysRole();
        existingRole.setId(1L);
        existingRole.setRoleName("管理员");
        existingRole.setRoleKey("admin");
        existingRole.setSort(0);
        existingRole.setStatus(1);
    }

    @Test
    void pageList_returnsPage() {
        when(baseMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(inv -> {
                    Page<SysRole> p = inv.getArgument(0);
                    p.setRecords(List.of(existingRole));
                    p.setTotal(1);
                    return p;
                });

        Page<SysRole> result = roleService.pageList(new Page<>(1, 10), "管理员");

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("admin", result.getRecords().get(0).getRoleKey());
    }

    @Test
    void getRolesByUserId_returnsRoles() {
        when(baseMapper.selectRolesByUserId(1L)).thenReturn(List.of(existingRole));

        List<SysRole> result = roleService.getRolesByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getRoleKey());
    }

    @Test
    void addRole_whenRoleKeyExists_throwsBusinessException() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        SysRoleDTO dto = new SysRoleDTO();
        dto.setRoleName("管理员");
        dto.setRoleKey("admin");
        dto.setMenuIds(Collections.emptyList());

        assertThrows(BusinessException.class, () -> roleService.addRole(dto));
        verify(baseMapper, never()).insert(any());
    }

    @Test
    void addRole_whenRoleKeyNotExists_savesAndReturnsId() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(SysRole.class))).thenAnswer(inv -> {
            SysRole r = inv.getArgument(0);
            r.setId(100L);
            return 1;
        });
        when(roleMenuMapper.insertBatch(anyList())).thenReturn(1);

        SysRoleDTO dto = new SysRoleDTO();
        dto.setRoleName("测试角色");
        dto.setRoleKey("test");
        dto.setSort(1);
        dto.setStatus(1);
        dto.setMenuIds(List.of(1L, 2L));

        Long id = roleService.addRole(dto);

        assertNotNull(id);
        assertEquals(100L, id);

        ArgumentCaptor<SysRole> roleCaptor = ArgumentCaptor.forClass(SysRole.class);
        verify(baseMapper).insert(roleCaptor.capture());
        SysRole saved = roleCaptor.getValue();
        assertEquals("测试角色", saved.getRoleName());
        assertEquals("test", saved.getRoleKey());
        assertEquals(1, saved.getSort());
        assertEquals(1, saved.getStatus());

        ArgumentCaptor<List<SysRoleMenu>> menuCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuMapper).insertBatch(menuCaptor.capture());
        assertEquals(2, menuCaptor.getValue().size());
    }

    @Test
    void updateRole_whenRoleNotExists_throwsBusinessException() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        SysRoleDTO dto = new SysRoleDTO();
        dto.setId(999L);
        dto.setRoleName("角色");
        dto.setRoleKey("role");

        assertThrows(BusinessException.class, () -> roleService.updateRole(dto));
        verify(baseMapper, never()).updateById(any());
    }

    @Test
    void updateRole_whenRoleExists_updatesAndSavesMenus() {
        when(baseMapper.selectById(1L)).thenReturn(existingRole);
        when(baseMapper.updateById(any(SysRole.class))).thenReturn(1);
        when(roleMenuMapper.deleteByRoleId(1L)).thenReturn(2);
        when(roleMenuMapper.insertBatch(anyList())).thenReturn(1);

        SysRoleDTO dto = new SysRoleDTO();
        dto.setId(1L);
        dto.setRoleName("新名称");
        dto.setRoleKey("admin");
        dto.setSort(2);
        dto.setStatus(0);
        dto.setMenuIds(List.of(1L));

        roleService.updateRole(dto);

        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(baseMapper).updateById(captor.capture());
        assertEquals("新名称", captor.getValue().getRoleName());
        assertEquals(2, captor.getValue().getSort());
        assertEquals(0, captor.getValue().getStatus());
        verify(roleMenuMapper).deleteByRoleId(1L);
        verify(roleMenuMapper).insertBatch(anyList());
    }

    @Test
    void deleteRole_removesRoleAndMenus() {
        when(baseMapper.deleteById(1L)).thenReturn(1);
        when(roleMenuMapper.deleteByRoleId(1L)).thenReturn(3);

        roleService.deleteRole(1L);

        verify(baseMapper).deleteById(1L);
        verify(roleMenuMapper).deleteByRoleId(1L);
    }
}
