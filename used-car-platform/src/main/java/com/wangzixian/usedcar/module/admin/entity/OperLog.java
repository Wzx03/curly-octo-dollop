package com.wangzixian.usedcar.module.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@TableName("sys_oper_log")
@Table(name="sys_oper_log")
public class OperLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;           // 模块标题
    private Integer businessType;   // 0-其他, 1-新增, 2-修改, 3-删除
    private String method;          // 方法名
    private String requestMethod;   // GET/POST
    private String operName;        // 操作人
    private String operUrl;         // URL
    private String operIp;          // IP
    
    @Column(columnDefinition = "TEXT")
    private String operParam;       // 请求参数
    
    @Column(columnDefinition = "TEXT")
    private String jsonResult;      // 返回结果
    
    private Integer status;         // 0-正常, 1-异常
    private String errorMsg;        // 错误信息
    private LocalDateTime operTime; // 操作时间
}