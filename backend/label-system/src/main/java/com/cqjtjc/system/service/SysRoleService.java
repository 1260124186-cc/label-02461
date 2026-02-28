package com.cqjtjc.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqjtjc.system.domain.dto.SysRoleDTO;
import com.cqjtjc.system.domain.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    Page<SysRole> pageList(Page<SysRole> page, String roleName);

    List<SysRole> getRolesByUserId(Long userId);

    Long addRole(SysRoleDTO dto);

    void updateRole(SysRoleDTO dto);

    void deleteRole(Long id);
}
