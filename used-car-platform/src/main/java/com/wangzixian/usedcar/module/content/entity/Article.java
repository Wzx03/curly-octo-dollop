package com.wangzixian.usedcar.module.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@TableName("sys_article")
@Table(name="sys_article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String cover;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String author;
    private Integer views;
    private LocalDateTime createTime;
}