package com.wangzixian.usedcar.module.user.controller;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.user.entity.Complaint;
import com.wangzixian.usedcar.module.user.mapper.ComplaintMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintMapper complaintMapper;

    private Long getUserId(String token) {
        return Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
    }

    /**
     * 提交投诉
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Complaint complaint, @RequestHeader("token") String token) {
        Long userId = getUserId(token);
        complaint.setUserId(userId);
        complaint.setStatus(0); // 默认待处理
        complaint.setCreateTime(LocalDateTime.now());
        
        complaintMapper.insert(complaint);
        return Result.success("投诉提交成功，管理员将在24小时内处理");
    }

    /**
     * 我的投诉列表
     */
    @GetMapping("/my-list")
    public Result<List<Complaint>> myList(@RequestHeader("token") String token) {
        Long userId = getUserId(token);
        
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getUserId, userId);
        wrapper.orderByDesc(Complaint::getCreateTime);
        
        return Result.success(complaintMapper.selectList(wrapper));
    }

    /**
     * 投诉详情
     */
    @GetMapping("/detail/{id}")
    public Result<Complaint> detail(@PathVariable Long id) {
        return Result.success(complaintMapper.selectById(id));
    }
}