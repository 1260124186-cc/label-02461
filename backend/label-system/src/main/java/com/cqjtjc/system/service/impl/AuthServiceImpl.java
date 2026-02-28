package com.cqjtjc.system.service.impl;

import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.common.utils.JwtUtils;
import com.cqjtjc.system.domain.dto.LoginDTO;
import com.cqjtjc.system.domain.entity.SysRole;
import com.cqjtjc.system.domain.entity.SysUser;
import com.cqjtjc.system.domain.vo.LoginVO;
import com.cqjtjc.system.service.AuthService;
import com.cqjtjc.system.service.SysMenuService;
import com.cqjtjc.system.service.SysRoleService;
import com.cqjtjc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 生成 token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = jwtUtils.generateToken(user.getUsername(), claims);

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(buildUserInfo(user));
        return loginVO;
    }

    @Override
    public LoginVO.UserInfoVO getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }
        String username = authentication.getName();
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserInfo(user);
    }

    private LoginVO.UserInfoVO buildUserInfo(SysUser user) {
        LoginVO.UserInfoVO userInfo = new LoginVO.UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());

        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        userInfo.setRoles(roles.stream().map(SysRole::getRoleKey).collect(Collectors.toList()));

        List<String> permissions = menuService.getPermissionsByUserId(user.getId());
        userInfo.setPermissions(permissions);

        return userInfo;
    }
}
