package com.cqjtjc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqjtjc.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /** 按用户ID删除关联，SQL 见 SysUserRoleMapper.xml */
    int deleteByUserId(@Param("userId") Long userId);

    /** 批量插入用户-角色关联，SQL 见 SysUserRoleMapper.xml */
    int insertBatch(@Param("list") List<SysUserRole> list);
}
