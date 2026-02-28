package com.cqjtjc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqjtjc.system.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /** 按角色ID删除关联，SQL 见 SysRoleMenuMapper.xml */
    int deleteByRoleId(@Param("roleId") Long roleId);
}
