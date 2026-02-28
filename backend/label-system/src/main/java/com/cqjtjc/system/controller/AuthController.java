package com.cqjtjc.system.controller;

import com.cqjtjc.common.result.R;
import com.cqjtjc.system.domain.dto.LoginDTO;
import com.cqjtjc.system.domain.vo.LoginVO;
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

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<LoginVO.UserInfoVO> info() {
        return R.ok(authService.getCurrentUserInfo());
    }
}
