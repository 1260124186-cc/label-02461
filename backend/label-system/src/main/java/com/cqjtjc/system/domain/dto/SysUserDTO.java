package com.cqjtjc.system.domain.dto;

import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户新增/修改请求
 */
@Data
public class SysUserDTO {

    @NotNull(message = "用户ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = AddGroup.class)
    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
