package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.system.dto.SysMenuDTO;
import com.cqjtjc.system.entity.SysMenu;
import com.cqjtjc.system.mapper.SysMenuMapper;
import com.cqjtjc.system.vo.MenuTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysMenuServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest {

    @Mock
    private SysMenuMapper baseMapper;

    @Mock
    private com.cqjtjc.system.mapper.SysRoleMenuMapper roleMenuMapper;

    @InjectMocks
    private SysMenuServiceImpl menuService;

    private SysMenu existingMenu;

    @BeforeEach
    void setUp() {
        existingMenu = new SysMenu();
        existingMenu.setId(1L);
        existingMenu.setParentId(0L);
        existingMenu.setMenuName("系统管理");
        existingMenu.setMenuType("M");
        existingMenu.setSort(0);
        existingMenu.setStatus(1);
    }

    @Test
    void getMenuTree_returnsTree() {
        when(baseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existingMenu));

        List<MenuTreeVO> result = menuService.getMenuTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("系统管理", result.get(0).getMenuName());
        assertEquals(0L, result.get(0).getParentId());
        assertNotNull(result.get(0).getChildren());
        assertTrue(result.get(0).getChildren().isEmpty());
    }

    @Test
    void getMenuTreeByUserId_returnsTree() {
        when(baseMapper.selectMenusByUserId(1L)).thenReturn(List.of(existingMenu));

        List<MenuTreeVO> result = menuService.getMenuTreeByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("系统管理", result.get(0).getMenuName());
    }

    @Test
    void getPermissionsByUserId_returnsPermissions() {
        when(baseMapper.selectPermissionsByUserId(1L)).thenReturn(List.of("system:user:list", "system:role:list"));

        List<String> result = menuService.getPermissionsByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("system:user:list"));
        assertTrue(result.contains("system:role:list"));
    }

    @Test
    void getMenuIdsByRoleId_returnsMenuIds() {
        when(baseMapper.selectMenusByRoleId(1L)).thenReturn(List.of(existingMenu));

        List<Long> result = menuService.getMenuIdsByRoleId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0));
    }

    @Test
    void addMenu_savesAndReturnsId() {
        when(baseMapper.insert(any(SysMenu.class))).thenAnswer(inv -> {
            SysMenu m = inv.getArgument(0);
            m.setId(100L);
            return 1;
        });

        SysMenuDTO dto = new SysMenuDTO();
        dto.setMenuName("用户管理");
        dto.setMenuType("C");
        dto.setParentId(1L);

        Long id = menuService.addMenu(dto);

        assertNotNull(id);
        assertEquals(100L, id);
        verify(baseMapper).insert(argThat(m -> "用户管理".equals(m.getMenuName()) && "C".equals(m.getMenuType()) && 1L == m.getParentId()));
    }

    @Test
    void addMenu_whenParentIdNull_setsToZero() {
        when(baseMapper.insert(any(SysMenu.class))).thenAnswer(inv -> {
            SysMenu m = inv.getArgument(0);
            m.setId(100L);
            return 1;
        });

        SysMenuDTO dto = new SysMenuDTO();
        dto.setMenuName("根菜单");
        dto.setMenuType("M");

        menuService.addMenu(dto);

        verify(baseMapper).insert(argThat(m -> 0L == m.getParentId()));
    }

    @Test
    void updateMenu_whenMenuNotExists_throwsBusinessException() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        SysMenuDTO dto = new SysMenuDTO();
        dto.setId(999L);
        dto.setMenuName("菜单");
        dto.setMenuType("C");

        assertThrows(BusinessException.class, () -> menuService.updateMenu(dto));
        verify(baseMapper, never()).updateById(any());
    }

    @Test
    void updateMenu_whenMenuExists_updates() {
        when(baseMapper.selectById(1L)).thenReturn(existingMenu);
        when(baseMapper.updateById(any(SysMenu.class))).thenReturn(1);

        SysMenuDTO dto = new SysMenuDTO();
        dto.setId(1L);
        dto.setMenuName("新名称");
        dto.setMenuType("C");

        menuService.updateMenu(dto);

        verify(baseMapper).updateById(argThat(m -> "新名称".equals(m.getMenuName())));
    }

    @Test
    void deleteMenu_whenHasChildren_throwsBusinessException() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertThrows(BusinessException.class, () -> menuService.deleteMenu(1L));
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    void deleteMenu_whenNoChildren_removes() {
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.deleteById(1L)).thenReturn(1);

        menuService.deleteMenu(1L);

        verify(baseMapper).deleteById(1L);
    }

    @Test
    void list_delegatesToBaseMapper() {
        when(baseMapper.selectList(any())).thenReturn(List.of(existingMenu));

        List<SysMenu> result = menuService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
