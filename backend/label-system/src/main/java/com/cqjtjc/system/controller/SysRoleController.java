package com.cqjtjc.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqjtjc.common.result.PageResult;
import com.cqjtjc.common.result.R;
import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import com.cqjtjc.system.dto.SysRoleDTO;
import com.cqjtjc.system.entity.SysRole;
import com.cqjtjc.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    /**
     * 分页查询角色。
     *
     * @param current 页码（默认 1）
     * @param size    每页条数（默认 10）
     * @param roleName 可选，按角色名称模糊查询
     */
    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public R<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName) {
        Page<SysRole> page = roleService.pageList(new Page<>(current, size), roleName);
        List<SysRole> records = page.getRecords() != null ? page.getRecords() : List.of();
        return R.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records));
    }

    /**
     * 查询所有角色（常用于下拉框等场景）。
     */
    @Operation(summary = "查询所有角色")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list());
    }

    /**
     * 获取角色详情。
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R<SysRole> getById(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    /**
     * 新增角色并绑定菜单/权限点。
     *
     * <p>{@code menuIds} 对应 {@code sys_menu.id}，用于将角色与菜单/按钮权限建立关联。</p>
     *
     * @return 新增角色 ID
     */
    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody SysRoleDTO dto) {
        Long id = roleService.addRole(dto);
        return R.ok(id);
    }

    /**
     * 修改角色信息并重建角色-菜单关联（先删后插）。
     */
    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysRoleDTO dto) {
        roleService.updateRole(dto);
        return R.ok();
    }

    /**
     * 删除角色并清理角色-菜单关联。
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public R<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.ok();
    }
}
