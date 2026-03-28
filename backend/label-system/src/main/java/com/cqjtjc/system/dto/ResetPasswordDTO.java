package com.cqjtjc.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "请设置新密码")
    @Size(min = 8, max = 128, message = "密码长度为 8~128 个字符")
    private String newPassword;
}
