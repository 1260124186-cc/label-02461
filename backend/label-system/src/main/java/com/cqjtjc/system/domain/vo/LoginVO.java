package com.cqjtjc.system.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
public class LoginVO {

    private String token;
    private UserInfoVO userInfo;

    @Data
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private List<String> roles;
        private List<String> permissions;
    }
}
