package com.wangzixian.usedcar.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@TableName("sys_complaint")
@Table(name="sys_complaint")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;        // 投诉人ID
    private Integer targetType; // 1-订单, 2-车辆, 3-用户
    private String targetId;    // 目标ID (可能是订单号String，也可能是车ID Long，统一存String)
    
    private String category;    // 投诉分类
    private String content;     // 详细描述
    private String images;      // 图片凭证 (逗号分隔)
    
    private Integer status;     // 0-待处理, 1-处理中, 2-已完结
    private String reply;       // 管理员回复
    
    private LocalDateTime createTime;
}