package com.cqjtjc.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.common.result.PageResult;
import com.cqjtjc.common.result.R;
import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
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

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody SysUserDTO dto) {
        Long id = userService.addUser(dto);
        return R.ok(id);
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysUserDTO dto) {
        userService.updateUser(dto);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/resetPassword")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return R.ok();
    }
}
