package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.system.dto.SysMenuDTO;
import com.cqjtjc.system.entity.SysMenu;
import com.cqjtjc.system.vo.MenuTreeVO;
import com.cqjtjc.system.mapper.SysMenuMapper;
import com.cqjtjc.system.mapper.SysRoleMenuMapper;
import com.cqjtjc.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<SysMenu> menus = this.list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));
        return buildTree(menus, 0L);
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildTree(menus, 0L);
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SysMenu> menus = baseMapper.selectMenusByRoleId(roleId);
        return menus.stream().map(SysMenu::getId).collect(Collectors.toList());
    }

    @Override
    public Long addMenu(SysMenuDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        this.save(menu);
        log.info("新增菜单成功, menuName={}, menuId={}", menu.getMenuName(), menu.getId());
        return menu.getId();
    }

    @Override
    public void updateMenu(SysMenuDTO dto) {
        SysMenu menu = this.getById(dto.getId());
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        BeanUtils.copyProperties(dto, menu);
        this.updateById(menu);
        log.info("修改菜单成功, menuId={}, menuName={}", menu.getId(), menu.getMenuName());
    }

    @Override
    public void deleteMenu(Long id) {
        // 检查是否有子菜单
        long count = this.count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (count > 0) {
            throw new BusinessException("存在子菜单，不允许删除");
        }
        this.removeById(id);
        log.info("删除菜单成功, menuId={}", id);
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> menus, Long parentId) {
        List<MenuTreeVO> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                MenuTreeVO vo = new MenuTreeVO();
                BeanUtils.copyProperties(menu, vo);
                vo.setChildren(buildTree(menus, menu.getId()));
                tree.add(vo);
            }
        }
        return tree;
    }
}
