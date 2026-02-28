package com.cqjtjc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqjtjc.system.dto.SysMenuDTO;
import com.cqjtjc.system.entity.SysMenu;
import com.cqjtjc.system.vo.MenuTreeVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单树
     */
    List<MenuTreeVO> getMenuTree();

    /**
     * 根据用户ID查询菜单树
     */
    List<MenuTreeVO> getMenuTreeByUserId(Long userId);

    /**
     * 根据用户ID查询权限标识
     */
    List<String> getPermissionsByUserId(Long userId);

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    Long addMenu(SysMenuDTO dto);

    void updateMenu(SysMenuDTO dto);

    void deleteMenu(Long id);
}
