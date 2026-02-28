package com.cqjtjc.system.controller;

import com.cqjtjc.common.result.R;
import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import com.cqjtjc.system.dto.SysMenuDTO;
import com.cqjtjc.system.entity.SysMenu;
import com.cqjtjc.system.vo.MenuTreeVO;
import com.cqjtjc.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<List<MenuTreeVO>> tree() {
        return R.ok(menuService.getMenuTree());
    }

    @Operation(summary = "查询所有菜单")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public R<List<SysMenu>> list() {
        return R.ok(menuService.list());
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<SysMenu> getById(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @Operation(summary = "根据角色ID查询菜单ID列表")
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        return R.ok(menuService.getMenuIdsByRoleId(roleId));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody SysMenuDTO dto) {
        Long id = menuService.addMenu(dto);
        return R.ok(id);
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysMenuDTO dto) {
        menuService.updateMenu(dto);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
