package com.wangzixian.usedcar.module.user.controller;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.user.entity.TransactionLog;
import com.wangzixian.usedcar.module.user.mapper.TransactionLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    private Long getUserId(String token) {
        return Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
    }

    /**
     * 获取我的资金流水
     */
    @GetMapping("/logs")
    public Result<List<TransactionLog>> logs(@RequestHeader("token") String token) {
        Long userId = getUserId(token);
        
        LambdaQueryWrapper<TransactionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionLog::getUserId, userId);
        wrapper.orderByDesc(TransactionLog::getCreateTime);
        
        return Result.success(transactionLogMapper.selectList(wrapper));
    }
}