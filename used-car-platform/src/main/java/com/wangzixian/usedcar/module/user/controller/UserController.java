package com.wangzixian.usedcar.module.user.controller;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.common.annotation.Log;
import com.wangzixian.usedcar.module.user.entity.TransactionLog;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.TransactionLogMapper;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionLogMapper transactionLogMapper; // 👈 注入

    private Long getUserId(String token) {
        return Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
    }

    @GetMapping("/info")
    public Result<User> info(@RequestHeader("token") String token) {
        Long userId = getUserId(token);
        User user = userMapper.selectById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @Log(title = "修改资料", businessType = 2)
    @PostMapping("/update")
    public Result<String> update(@RequestBody User user, @RequestHeader("token") String token) {
        Long userId = getUserId(token);
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
               .set(user.getNickname() != null, User::getNickname, user.getNickname())
               .set(user.getAvatar() != null, User::getAvatar, user.getAvatar())
               .set(user.getPhone() != null, User::getPhone, user.getPhone());
               
        userMapper.update(null, wrapper);
        return Result.success("修改成功");
    }

    @Log(title = "修改密码", businessType = 2)
    @PostMapping("/password")
    public Result<String> password(@RequestBody Map<String, String> params, @RequestHeader("token") String token) {
        Long userId = getUserId(token);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        User user = userMapper.selectById(userId);
        
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        user.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(user);
        
        return Result.success("密码修改成功，请重新登录");
    }

    @Log(title = "余额充值", businessType = 2)
    @PostMapping("/recharge")
    public Result<String> recharge(@RequestBody Map<String, Double> params, @RequestHeader("token") String token) {
        Long userId = getUserId(token);
        Double amount = params.get("amount");

        if (amount == null || amount <= 0) {
            return Result.error("金额必须大于0");
        }

        User user = userMapper.selectById(userId);
        Double currentBalance = user.getBalance() == null ? 0.0 : user.getBalance();
        Double newBalance = currentBalance + amount;
        user.setBalance(newBalance);
        
        userMapper.updateById(user);

        // 👇 记录流水
        TransactionLog log = new TransactionLog();
        log.setUserId(userId);
        log.setType(1); // 充值
        log.setAmount(BigDecimal.valueOf(amount));
        log.setBalanceAfter(BigDecimal.valueOf(newBalance));
        log.setRemark("余额充值");
        log.setCreateTime(LocalDateTime.now());
        transactionLogMapper.insert(log);

        return Result.success("充值成功，当前余额：" + user.getBalance() + "万");
    }
}