package com.cqjtjc.system.dto;

import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色新增/修改请求
 */
@Data
public class SysRoleDTO {

    @NotNull(message = "角色ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotBlank(message = "角色名称不能为空", groups = AddGroup.class)
    private String roleName;

    @NotBlank(message = "角色标识不能为空", groups = AddGroup.class)
    private String roleKey;

    private Integer sort;

    private Integer status;

    private String remark;

    /** 菜单ID列表 */
    private List<Long> menuIds;
}
