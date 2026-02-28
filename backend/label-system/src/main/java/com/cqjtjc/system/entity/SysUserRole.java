package com.cqjtjc.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户-角色关联表
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    private Long userId;
    private Long roleId;
}
