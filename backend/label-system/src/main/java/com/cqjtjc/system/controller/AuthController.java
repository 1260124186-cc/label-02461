package com.cqjtjc.system.controller;

import com.cqjtjc.common.result.R;
import com.cqjtjc.system.dto.LoginDTO;
import com.cqjtjc.system.vo.LoginVO;
import com.cqjtjc.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录获取 JWT Token。
     *
     * <p>成功后返回的 {@code token} 需要在后续请求中通过 Header 携带：
     * {@code Authorization: Bearer <token>}。</p>
     *
     * <p>失败场景由全局异常处理统一返回：
     * 账号/密码错误返回 401；用户不存在/禁用等业务问题返回对应业务码。</p>
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /**
     * 获取当前登录用户信息（含角色标识与权限点列表）。
     *
     * <p>用于前端初始化：展示用户信息、构建动态路由/按钮权限等。</p>
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<LoginVO.UserInfoVO> info() {
        return R.ok(authService.getCurrentUserInfo());
    }
}
