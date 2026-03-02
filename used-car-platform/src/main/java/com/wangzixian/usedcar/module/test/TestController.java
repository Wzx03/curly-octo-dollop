package com.wangzixian.usedcar.module.test;

import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserMapper userMapper; // 注入刚才写的 Mapper 接口

    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        // selectList(null) 是 MyBatis-Plus 提供的内置方法，表示查询所有
        List<User> users = userMapper.selectList(null);
        return Result.success(users);
    }
}