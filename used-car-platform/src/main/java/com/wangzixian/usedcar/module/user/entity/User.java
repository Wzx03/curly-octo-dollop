package com.wangzixian.usedcar.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表 sys_user
 */
@Data
@Entity
@TableName("sys_user")
@Table(name="sys_user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private Integer role; // 0-管理员, 1-卖家, 2-买家
    private String avatar;
    private Double balance;
    
    // 👇 新增手机号字段
    private String phone;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 逻辑删除字段 (查询时会自动忽略该值为 1 的记录)
    private Integer deleted;
}