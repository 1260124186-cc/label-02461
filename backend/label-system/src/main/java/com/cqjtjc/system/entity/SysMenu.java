package com.cqjtjc.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单/权限表
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String permission;

    /** 菜单类型 M-目录 C-菜单 F-按钮 */
    private String menuType;

    /** 菜单图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态 0-禁用 1-正常 */
    private Integer status;

    /** 是否可见 0-隐藏 1-显示 */
    private Integer visible;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
