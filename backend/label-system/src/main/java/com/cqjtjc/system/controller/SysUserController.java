package com.cqjtjc.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.common.result.PageResult;
import com.cqjtjc.common.result.R;
import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import com.cqjtjc.system.dto.ResetPasswordDTO;
import com.cqjtjc.system.dto.SysUserDTO;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    /**
     * 分页查询用户列表。
     *
     * <p>注意：为避免敏感信息泄露，本接口会将返回记录中的 {@code password} 置空。</p>
     *
     * @param current 页码（默认 1）
     * @param size    每页条数（默认 10）
     * @param username 可选，按用户名模糊查询
     * @param status  可选，按状态过滤（0/1）
     */
    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public R<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        Page<SysUser> page = userService.pageList(new Page<>(current, size), username, status);
        List<SysUser> records = page.getRecords() != null ? page.getRecords() : List.of();
        records.forEach(u -> u.setPassword(null));
        return R.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records));
    }

    /**
     * 获取用户详情。
     *
     * <p>同样会将 {@code password} 置空，避免返回敏感字段。</p>
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return R.ok(user);
    }

    /**
     * 新增用户。
     *
     * <p>使用 {@link AddGroup} 进行参数校验；密码需满足强度要求（长度 8~128 且包含字母与数字）。</p>
     *
     * @return 新增用户 ID
     */
    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody SysUserDTO dto) {
        Long id = userService.addUser(dto);
        return R.ok(id);
    }

    /**
     * 修改用户。
     *
     * <p>使用 {@link UpdateGroup} 校验；更新基础信息的同时会重置用户-角色关联（先删后插）。</p>
     */
    @Operation(summary = "修改用户")
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysUserDTO dto) {
        userService.updateUser(dto);
        return R.ok();
    }

    /**
     * 删除用户（逻辑删除）并清理用户-角色关联。
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    /**
     * 重置指定用户密码。
     *
     * <p>新密码同样需要满足强度要求；实际加密由 {@code PasswordEncoder} 处理。</p>
     */
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/resetPassword")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public R<Void> resetPassword(@PathVariable Long id, @Validated @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(id, dto.getNewPassword());
        return R.ok();
    }
}
