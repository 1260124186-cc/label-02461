package com.cqjtjc.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.system.dto.SysRoleDTO;
import com.cqjtjc.system.entity.SysRole;
import com.cqjtjc.system.service.SysRoleService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SysRoleController 单元测试
 */
@WebMvcTest(SysRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SysRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SysRoleService roleService;

    @Test
    @WithMockUser(authorities = "system:role:list")
    void page_returnsPageResult() throws Exception {
        Page<SysRole> page = new Page<>(1, 10);
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleKey("admin");
        role.setRoleName("管理员");
        page.setRecords(List.of(role));
        page.setTotal(1);

        when(roleService.pageList(any(Page.class), any())).thenReturn(page);

        mockMvc.perform(get("/system/role/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(authorities = "system:role:query")
    void list_returnsRoles() throws Exception {
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleKey("admin");
        when(roleService.list()).thenReturn(List.of(role));

        mockMvc.perform(get("/system/role/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].roleKey").value("admin"));
    }

    @Test
    @WithMockUser(authorities = "system:role:query")
    void getById_returnsRole() throws Exception {
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleKey("admin");
        role.setRoleName("管理员");
        when(roleService.getById(1L)).thenReturn(role);

        mockMvc.perform(get("/system/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.roleKey").value("admin"));
    }

    @Test
    @WithMockUser(authorities = "system:role:add")
    void add_returnsId() throws Exception {
        when(roleService.addRole(any(SysRoleDTO.class))).thenReturn(100L);

        SysRoleDTO dto = new SysRoleDTO();
        dto.setRoleName("测试角色");
        dto.setRoleKey("test");

        mockMvc.perform(post("/system/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @WithMockUser(authorities = "system:role:edit")
    void update_succeeds() throws Exception {
        doNothing().when(roleService).updateRole(any(SysRoleDTO.class));

        SysRoleDTO dto = new SysRoleDTO();
        dto.setId(1L);
        dto.setRoleName("新名称");
        dto.setRoleKey("admin");

        mockMvc.perform(put("/system/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(authorities = "system:role:delete")
    void delete_succeeds() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/system/role/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
