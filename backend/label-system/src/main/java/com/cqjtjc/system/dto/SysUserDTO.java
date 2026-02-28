package com.cqjtjc.system.dto;

import com.cqjtjc.common.validation.AddGroup;
import com.cqjtjc.common.validation.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 50, message = "用户名长度为 2~50 个字符", groups = AddGroup.class)
    private String username;

    @NotBlank(message = "请设置密码", groups = AddGroup.class)
    @Size(min = 8, max = 128, message = "密码长度为 8~128 个字符", groups = AddGroup.class)
    private String password;

    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过 100 个字符")
    private String email;

    @Size(max = 20, message = "手机号不能超过 20 个字符")
    private String phone;

    private Integer status;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
