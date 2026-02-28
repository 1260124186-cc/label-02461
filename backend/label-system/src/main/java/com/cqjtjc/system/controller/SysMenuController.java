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

    /**
     * 查询菜单树（从根节点 parentId=0 开始）。
     *
     * <p>主要用于前端构建菜单结构；返回的树节点为 {@link MenuTreeVO}。</p>
     */
    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<List<MenuTreeVO>> tree() {
        return R.ok(menuService.getMenuTree());
    }

    /**
     * 查询所有菜单（扁平结构）。
     *
     * <p>常用于菜单管理页、权限点选择等场景。</p>
     */
    @Operation(summary = "查询所有菜单")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public R<List<SysMenu>> list() {
        return R.ok(menuService.list());
    }

    /**
     * 获取菜单详情。
     */
    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<SysMenu> getById(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    /**
     * 根据角色 ID 查询该角色拥有的菜单/权限点 ID 列表。
     *
     * <p>用于角色授权时回显勾选项。</p>
     */
    @Operation(summary = "根据角色ID查询菜单ID列表")
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        return R.ok(menuService.getMenuIdsByRoleId(roleId));
    }

    /**
     * 新增菜单/权限点。
     *
     * <p>当 {@code menuType=F}（按钮）时，通常会填写 {@code permission} 作为接口权限点，
     * 与 {@code @PreAuthorize("hasAuthority('xxx')")} 的 {@code xxx} 对应。</p>
     *
     * @return 新增菜单 ID
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody SysMenuDTO dto) {
        Long id = menuService.addMenu(dto);
        return R.ok(id);
    }

    /**
     * 修改菜单。
     */
    @Operation(summary = "修改菜单")
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysMenuDTO dto) {
        menuService.updateMenu(dto);
        return R.ok();
    }

    /**
     * 删除菜单。
     *
     * <p>若存在子菜单会拒绝删除（业务异常），避免树结构被破坏。</p>
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
