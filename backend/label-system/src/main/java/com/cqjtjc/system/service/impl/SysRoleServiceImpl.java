package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.system.domain.dto.SysRoleDTO;
import com.cqjtjc.system.domain.entity.SysRole;
import com.cqjtjc.system.domain.entity.SysRoleMenu;
import com.cqjtjc.system.mapper.SysRoleMapper;
import com.cqjtjc.system.mapper.SysRoleMenuMapper;
import com.cqjtjc.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public Page<SysRole> pageList(Page<SysRole> page, String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByAsc(SysRole::getSort);
        return this.page(page, wrapper);
    }

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRole(SysRoleDTO dto) {
        // 检查角色标识是否存在
        long count = this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, dto.getRoleKey()));
        if (count > 0) {
            throw new BusinessException("角色标识已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort() != null ? dto.getSort() : 0);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setRemark(dto.getRemark());
        LocalDateTime now = LocalDateTime.now();
        role.setCreateTime(now);
        role.setUpdateTime(now);
        role.setDeleted(0);
        this.save(role);

        Long roleId = role.getId();
        if (roleId == null) {
            SysRole saved = this.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, dto.getRoleKey()).last("LIMIT 1"));
            if (saved != null) {
                roleId = saved.getId();
            }
        }
        if (roleId != null) {
            saveRoleMenus(roleId, dto.getMenuIds());
        }
        return roleId != null ? roleId : role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRoleDTO dto) {
        SysRole role = this.getById(dto.getId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        role.setRemark(dto.getRemark());
        this.updateById(role);

        roleMenuMapper.deleteByRoleId(role.getId());
        saveRoleMenus(role.getId(), dto.getMenuIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        this.removeById(id);
        roleMenuMapper.deleteByRoleId(id);
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null || CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }
}
