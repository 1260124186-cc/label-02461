package com.cqjtjc.system.controller;

import com.cqjtjc.system.dto.LoginDTO;
import com.cqjtjc.system.service.AuthService;
import com.cqjtjc.system.vo.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void login_whenValid_returnsOkWithToken() throws Exception {
        LoginVO vo = new LoginVO();
        vo.setToken("jwt-token");
        LoginVO.UserInfoVO userInfo = new LoginVO.UserInfoVO();
        userInfo.setId(1L);
        userInfo.setUsername("admin");
        userInfo.setNickname("管理员");
        vo.setUserInfo(userInfo);

        when(authService.login(any(LoginDTO.class))).thenReturn(vo);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("password");

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.userInfo.username").value("admin"));
    }

    @Test
    @WithMockUser
    void info_whenAuthenticated_returnsUserInfo() throws Exception {
        LoginVO.UserInfoVO userInfo = new LoginVO.UserInfoVO();
        userInfo.setId(1L);
        userInfo.setUsername("admin");
        userInfo.setNickname("管理员");

        when(authService.getCurrentUserInfo()).thenReturn(userInfo);

        mockMvc.perform(get("/auth/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }
}
