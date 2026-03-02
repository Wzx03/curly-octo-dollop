package com.wangzixian.usedcar.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.user.entity.Complaint;
import com.wangzixian.usedcar.module.user.mapper.ComplaintMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/complaint")
public class AdminComplaintController {

    @Autowired
    private ComplaintMapper complaintMapper;

    /**
     * 1. 投诉列表 (分页 + 筛选)
     */
    @GetMapping("/list")
    public Result<Page<Complaint>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status // 0-待处理, 1-已处理
    ) {
        Page<Complaint> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getCreateTime); // 新的在前面

        return Result.success(complaintMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 2. 处理投诉
     */
    @PostMapping("/handle")
    public Result<String> handle(@RequestBody Complaint form) {
        Complaint complaint = complaintMapper.selectById(form.getId());
        if (complaint == null) return Result.error("投诉不存在");

        complaint.setReply(form.getReply()); // 管理员回复内容
        complaint.setStatus(2); // 标记为已完结
        
        complaintMapper.updateById(complaint);
        
        return Result.success("处理完成");
    }
}