package com.wangzixian.usedcar.module.auth.controller;

import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.auth.dto.LoginDTO;
import com.wangzixian.usedcar.module.auth.dto.RegisterDTO;
import com.wangzixian.usedcar.module.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 定义统一入口路径
public class AuthController {

    @Autowired
    private AuthService authService;


    /**
     * 登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        // 调用业务层，获取 Token
        String token = authService.login(loginDTO);
        // 返回给前端
        return Result.success(token);
    }

    /**
     * 注册接口
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success("注册成功，请登录");
    }
}