package com.cqjtjc.system.service.impl;

import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.common.exception.ErrorCode;
import com.cqjtjc.common.utils.JwtUtils;
import com.cqjtjc.system.dto.LoginDTO;
import com.cqjtjc.system.entity.SysRole;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.vo.LoginVO;
import com.cqjtjc.system.service.AuthService;
import com.cqjtjc.system.service.SysMenuService;
import com.cqjtjc.system.service.SysRoleService;
import com.cqjtjc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;

    @Override
    /**
     * 登录并签发 JWT。
     *
     * <p>流程说明：</p>
     * <ul>
     *   <li>先交给 {@link AuthenticationManager} 做账号/密码认证（失败会抛出 401）。</li>
     *   <li>再校验用户是否存在/是否禁用（业务异常）。</li>
     *   <li>签发 JWT：在 claims 中写入 {@code userId} 便于后续鉴权/审计扩展。</li>
     *   <li>返回的用户信息会附带角色标识（roleKey）与权限点（permission）列表，供前端初始化使用。</li>
     * </ul>
     */
    public LoginVO login(LoginDTO dto) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 生成 token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = jwtUtils.generateToken(user.getUsername(), claims);

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(buildUserInfo(user));
        log.info("用户登录成功, username={}, userId={}", user.getUsername(), user.getId());
        return loginVO;
    }

    @Override
    /**
     * 获取当前登录用户信息。
     *
     * <p>从 {@link SecurityContextHolder} 获取认证信息，并基于用户名加载用户、角色、权限点。</p>
     *
     * @throws BusinessException 未登录（401）或用户不存在等业务异常
     */
    public LoginVO.UserInfoVO getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        log.debug("获取当前用户信息, username={}", username);
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
