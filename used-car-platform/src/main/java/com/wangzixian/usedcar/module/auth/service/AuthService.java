package com.wangzixian.usedcar.module.auth.service;

import com.wangzixian.usedcar.module.auth.dto.LoginDTO;
import com.wangzixian.usedcar.module.auth.dto.RegisterDTO;

public interface AuthService {
    /**
     * 登录业务
     * @param loginDTO 登录参数
     * @return 登录成功后的 Token
     */
    String login(LoginDTO loginDTO);

    /**
     * 注册业务
     * @param registerDTO 注册参数
     */
    void register(RegisterDTO registerDTO);
}