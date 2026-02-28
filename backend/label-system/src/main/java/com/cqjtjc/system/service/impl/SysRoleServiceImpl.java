package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.common.exception.ErrorCode;
import com.cqjtjc.common.exception.ErrorCode;
import com.cqjtjc.common.exception.ErrorCode;
import com.cqjtjc.system.dto.SysRoleDTO;
import com.cqjtjc.system.entity.SysRole;
import com.cqjtjc.system.entity.SysRoleMenu;
import com.cqjtjc.system.mapper.SysRoleMapper;
import com.cqjtjc.system.mapper.SysRoleMenuMapper;
import com.cqjtjc.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    /**
     * 角色分页查询（按角色名模糊匹配，按 sort 升序）。
     */
    public Page<SysRole> pageList(Page<SysRole> page, String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByAsc(SysRole::getSort);
        return this.page(page, wrapper);
    }

    @Override
    /**
     * 根据用户 ID 查询角色列表（由 Mapper XML 提供联表 SQL）。
     *
     * <p>该结果通常用于登录后返回 roleKey 列表与鉴权逻辑。</p>
     */
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 新增角色并绑定菜单/权限点。
     *
     * <p>约束：{@code roleKey} 必须唯一；若 {@code menuIds} 为空则仅创建角色不写关联。</p>
     *
     * @return 新增角色 ID
     */
    public Long addRole(SysRoleDTO dto) {
        // 检查角色标识是否存在
        long count = this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, dto.getRoleKey()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.ROLE_KEY_EXISTS);
        }

        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort() != null ? dto.getSort() : 0);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setRemark(dto.getRemark());
        this.save(role);

        Long roleId = role.getId();
        saveRoleMenus(roleId, dto.getMenuIds());
        log.info("新增角色成功, roleKey={}, roleId={}", dto.getRoleKey(), roleId);
        return roleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 修改角色并重建角色-菜单关联。
     *
     * <p>关联更新采用“先删后插”策略，避免增量更新带来的差异状态。</p>
     */
    public void updateRole(SysRoleDTO dto) {
        SysRole role = this.getById(dto.getId());
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        role.setRemark(dto.getRemark());
        this.updateById(role);

        roleMenuMapper.deleteByRoleId(role.getId());
        saveRoleMenus(role.getId(), dto.getMenuIds());
        log.info("修改角色成功, roleId={}, roleKey={}", role.getId(), role.getRoleKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除角色并清理角色-菜单关联。
     */
    public void deleteRole(Long id) {
        this.removeById(id);
        roleMenuMapper.deleteByRoleId(id);
        log.info("删除角色成功, roleId={}", id);
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null || CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        List<SysRoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> {
                    SysRoleMenu rm = new SysRoleMenu();
                    rm.setRoleId(roleId);
                    rm.setMenuId(menuId);
                    return rm;
                })
                .collect(Collectors.toList());
        roleMenuMapper.insertBatch(roleMenus);
    }
}
