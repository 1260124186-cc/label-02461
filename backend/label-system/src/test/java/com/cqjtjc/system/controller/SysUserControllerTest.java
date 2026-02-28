package com.cqjtjc.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.system.dto.SysUserDTO;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.service.SysUserService;
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
 * SysUserController 单元测试
 */
@WebMvcTest(SysUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SysUserService userService;

    @Test
    @WithMockUser(authorities = "system:user:list")
    void page_returnsPageResult() throws Exception {
        Page<SysUser> page = new Page<>(1, 10);
        page.setRecords(List.of(createUser(1L, "admin")));
        page.setTotal(1);

        when(userService.pageList(any(Page.class), any(), any())).thenReturn(page);

        mockMvc.perform(get("/system/user/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(authorities = "system:user:query")
    void getById_returnsUser() throws Exception {
        SysUser user = createUser(1L, "admin");
        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/system/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "system:user:add")
    void add_returnsId() throws Exception {
        when(userService.addUser(any(SysUserDTO.class))).thenReturn(100L);

        SysUserDTO dto = new SysUserDTO();
        dto.setUsername("newuser");
        dto.setPassword("Pass1234");
        dto.setNickname("新用户");

        mockMvc.perform(post("/system/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @WithMockUser(authorities = "system:user:edit")
    void update_succeeds() throws Exception {
        doNothing().when(userService).updateUser(any(SysUserDTO.class));

        SysUserDTO dto = new SysUserDTO();
        dto.setId(1L);
        dto.setNickname("新昵称");
        dto.setStatus(1);

        mockMvc.perform(put("/system/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(authorities = "system:user:delete")
    void delete_succeeds() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/system/user/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(authorities = "system:user:resetPwd")
    void resetPassword_succeeds() throws Exception {
        doNothing().when(userService).resetPassword(eq(1L), eq("NewPass1"));

        mockMvc.perform(put("/system/user/1/resetPassword")
                        .with(csrf())
                        .param("newPassword", "NewPass1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private static SysUser createUser(Long id, String username) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setUsername(username);
        u.setNickname("管理员");
        return u;
    }
}
