package com.cqjtjc.system.dto;

import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单新增/修改请求
 */
@Data
public class SysMenuDTO {

    @NotNull(message = "菜单ID不能为空", groups = UpdateGroup.class)
    private Long id;

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空", groups = AddGroup.class)
    private String menuName;

    private String path;

    private String component;

    private String permission;

    @NotBlank(message = "菜单类型不能为空", groups = AddGroup.class)
    private String menuType;

    private String icon;

    private Integer sort;

    private Integer status;

    private Integer visible;
}
