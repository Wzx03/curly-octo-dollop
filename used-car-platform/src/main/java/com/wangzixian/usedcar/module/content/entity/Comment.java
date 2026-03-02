package com.wangzixian.usedcar.module.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_comment")
@TableName("sys_comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private Long userId;
    private String nickname; // 冗余存一下，方便查询
    private String avatar;   // 冗余存一下
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private LocalDateTime createTime;
}