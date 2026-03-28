package com.cqjtjc.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置密码 DTO
 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
