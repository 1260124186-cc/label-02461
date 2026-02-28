package com.cqjtjc.system.controller;

import com.cqjtjc.system.dto.SysMenuDTO;
import com.cqjtjc.system.entity.SysMenu;
import com.cqjtjc.system.service.SysMenuService;
import com.cqjtjc.system.vo.MenuTreeVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SysMenuController 单元测试
 */
@WebMvcTest(SysMenuController.class)
@AutoConfigureMockMvc(addFilters = false)
class SysMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SysMenuService menuService;

    @Test
    @WithMockUser(authorities = "system:menu:query")
    void tree_returnsMenuTree() throws Exception {
        MenuTreeVO node = new MenuTreeVO();
        node.setId(1L);
        node.setMenuName("系统管理");
        when(menuService.getMenuTree()).thenReturn(List.of(node));

        mockMvc.perform(get("/system/menu/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].menuName").value("系统管理"));
    }

    @Test
    @WithMockUser(authorities = "system:menu:list")
    void list_returnsMenuList() throws Exception {
        SysMenu menu = new SysMenu();
        menu.setId(1L);
        menu.setMenuName("用户管理");
        when(menuService.list()).thenReturn(List.of(menu));

        mockMvc.perform(get("/system/menu/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].menuName").value("用户管理"));
    }

    @Test
    @WithMockUser(authorities = "system:menu:query")
    void getById_returnsMenu() throws Exception {
        SysMenu menu = new SysMenu();
        menu.setId(1L);
        menu.setMenuName("用户管理");
        when(menuService.getById(1L)).thenReturn(menu);

        mockMvc.perform(get("/system/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "system:menu:query")
    void getMenuIdsByRoleId_returnsIds() throws Exception {
        when(menuService.getMenuIdsByRoleId(1L)).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get("/system/menu/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "system:menu:add")
    void add_returnsId() throws Exception {
        when(menuService.addMenu(any(SysMenuDTO.class))).thenReturn(100L);

        SysMenuDTO dto = new SysMenuDTO();
        dto.setMenuName("新菜单");
        dto.setMenuType("C");

        mockMvc.perform(post("/system/menu")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @WithMockUser(authorities = "system:menu:edit")
    void update_succeeds() throws Exception {
        doNothing().when(menuService).updateMenu(any(SysMenuDTO.class));

        SysMenuDTO dto = new SysMenuDTO();
        dto.setId(1L);
        dto.setMenuName("新名称");
        dto.setMenuType("C");

        mockMvc.perform(put("/system/menu")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(authorities = "system:menu:delete")
    void delete_succeeds() throws Exception {
        doNothing().when(menuService).deleteMenu(1L);

        mockMvc.perform(delete("/system/menu/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
