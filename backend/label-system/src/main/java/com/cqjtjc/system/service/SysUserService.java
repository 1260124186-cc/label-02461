package com.cqjtjc.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqjtjc.system.domain.dto.SysUserDTO;
import com.cqjtjc.system.domain.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     */
    Page<SysUser> pageList(Page<SysUser> page, String username, Integer status);

    /**
     * 根据用户名查询
     */
    SysUser getByUsername(String username);

    /**
     * 新增用户
     */
    Long addUser(SysUserDTO dto);

    /**
     * 修改用户
     */
    void updateUser(SysUserDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);
}
