package com.cqjtjc.system.service.impl;

import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.common.utils.JwtUtils;
import com.cqjtjc.system.dto.LoginDTO;
import com.cqjtjc.system.entity.SysRole;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.service.SysMenuService;
import com.cqjtjc.system.service.SysRoleService;
import com.cqjtjc.system.service.SysUserService;
import com.cqjtjc.system.vo.LoginVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SysUserService userService;

    @Mock
    private SysRoleService roleService;

    @Mock
    private SysMenuService menuService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser testUser;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setNickname("管理员");
        testUser.setStatus(1);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_whenSuccess_returnsLoginVO() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userService.getByUsername("admin")).thenReturn(testUser);
        when(roleService.getRolesByUserId(1L)).thenReturn(List.of(createRole("admin")));
        when(menuService.getPermissionsByUserId(1L)).thenReturn(List.of("system:user:list"));
        when(jwtUtils.generateToken(eq("admin"), anyMap())).thenReturn("jwt-token");

        LoginVO result = authService.login(loginDTO);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUserInfo());
        assertEquals(1L, result.getUserInfo().getId());
        assertEquals("admin", result.getUserInfo().getUsername());
        assertEquals("管理员", result.getUserInfo().getNickname());
        assertTrue(result.getUserInfo().getRoles().contains("admin"));
        assertTrue(result.getUserInfo().getPermissions().contains("system:user:list"));
    }

    @Test
    void login_whenUserNotFound_throwsBusinessException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userService.getByUsername("admin")).thenReturn(null);

        assertThrows(BusinessException.class, () -> authService.login(loginDTO));
    }

    @Test
    void login_whenUserDisabled_throwsBusinessException() {
        testUser.setStatus(0);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userService.getByUsername("admin")).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> authService.login(loginDTO));
    }

    @Test
    void getCurrentUserInfo_whenAuthenticated_returnsUserInfo() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        SecurityContextHolder.setContext(securityContext);

        when(userService.getByUsername("admin")).thenReturn(testUser);
        when(roleService.getRolesByUserId(1L)).thenReturn(List.of(createRole("admin")));
        when(menuService.getPermissionsByUserId(1L)).thenReturn(List.of("system:user:list"));

        LoginVO.UserInfoVO result = authService.getCurrentUserInfo();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin", result.getUsername());
        assertEquals("管理员", result.getNickname());
    }

    @Test
    void getCurrentUserInfo_whenNotAuthenticated_throwsBusinessException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(BusinessException.class, () -> authService.getCurrentUserInfo());
    }

    @Test
    void getCurrentUserInfo_whenUserNotFound_throwsBusinessException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("unknown");
        SecurityContextHolder.setContext(securityContext);
        when(userService.getByUsername("unknown")).thenReturn(null);

        assertThrows(BusinessException.class, () -> authService.getCurrentUserInfo());
    }

    private static SysRole createRole(String roleKey) {
        SysRole r = new SysRole();
        r.setRoleKey(roleKey);
        return r;
    }
}
