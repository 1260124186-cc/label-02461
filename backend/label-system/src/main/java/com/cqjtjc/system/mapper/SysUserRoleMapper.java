package com.cqjtjc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqjtjc.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /** 按用户ID删除关联，SQL 见 SysUserRoleMapper.xml */
    int deleteByUserId(@Param("userId") Long userId);
}
