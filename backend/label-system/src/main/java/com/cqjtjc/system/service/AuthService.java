package com.cqjtjc.system.service;

import com.cqjtjc.system.domain.dto.LoginDTO;
import com.cqjtjc.system.domain.vo.LoginVO;

public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    LoginVO.UserInfoVO getCurrentUserInfo();
}
