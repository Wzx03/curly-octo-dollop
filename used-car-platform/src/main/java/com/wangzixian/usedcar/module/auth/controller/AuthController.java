package com.wangzixian.usedcar.module.auth.controller;

import cn.hutool.core.util.IdUtil;
import com.wangzixian.usedcar.common.JwtUtils;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.auth.dto.LoginDTO;
import com.wangzixian.usedcar.module.auth.dto.RegisterDTO;
import com.wangzixian.usedcar.module.auth.service.AuthService;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 定义统一入口路径
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtils jwtUtils;


    /**
     * 登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<String> login(@Validated @RequestBody LoginDTO loginDTO) {
        // 调用业务层，获取 Token
        String token = authService.login(loginDTO);
        // 返回给前端
        return Result.success(token);
    }

    /*
    * 游客登陆接口
    * post
    * */
    @PostMapping("/guestLogin")
    public Result<String> guestLogin() {
        //使用uuid生成游客标识
        String uuid = IdUtil.fastSimpleUUID().substring(0, 8);
        String guestUsername = "guest_" + uuid;
        //构建游客实体对象
        User guestUser = new User();
        guestUser.setUsername(guestUsername);
        guestUser.setNickname("游客_" + uuid);
        guestUser.setRole(3);
        guestUser.setBalance(0.00);
        guestUser.setStatus(1);
        //插入数据库
        userMapper.insert(guestUser);
        String token = jwtUtils.generateToken(guestUser.getId(), guestUser.getRole());
        //返回前端
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