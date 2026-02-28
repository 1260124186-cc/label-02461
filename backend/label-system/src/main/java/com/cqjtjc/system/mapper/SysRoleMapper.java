package com.cqjtjc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqjtjc.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 根据用户ID查询角色列表，SQL 见 SysRoleMapper.xml */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
