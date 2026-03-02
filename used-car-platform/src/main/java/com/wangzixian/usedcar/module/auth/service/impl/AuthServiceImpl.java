package com.wangzixian.usedcar.module.auth.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangzixian.usedcar.common.JwtUtils;
import com.wangzixian.usedcar.module.auth.dto.LoginDTO;
import com.wangzixian.usedcar.module.auth.dto.RegisterDTO;
import com.wangzixian.usedcar.module.auth.service.AuthService;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String login(LoginDTO loginDTO) {
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new RuntimeException("账号或密码不能为空");
        }

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", loginDTO.getUsername());
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }

        boolean check = BCrypt.checkpw(loginDTO.getPassword(), user.getPassword());
        if (!check) {
            throw new RuntimeException("密码错误");
        }

        // 👇 传入 role
        return JwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterDTO dto) {
        if (dto.getUsername() == null || dto.getPassword() == null) {
            throw new RuntimeException("账号或密码不能为空");
        }

        if (!dto.getUsername().matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new RuntimeException("账号格式错误：需4-20位字母或数字");
        }
        if (!dto.getPassword().matches("^(?=.*[a-zA-Z])(?=.*[0-9]).{6,20}$")) {
            throw new RuntimeException("密码太简单：需6-20位，且包含字母和数字");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (!ReUtil.isMatch("^1\\d{10}$", dto.getPhone())) {
                throw new RuntimeException("手机号格式不正确");
            }
        }

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", dto.getUsername());
        if (userMapper.selectCount(query) > 0) {
            throw new RuntimeException("该账号已存在，请直接登录");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname("用户" + dto.getUsername());
        user.setRole(2); // 2-买家
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        userMapper.insert(user);
    }
}