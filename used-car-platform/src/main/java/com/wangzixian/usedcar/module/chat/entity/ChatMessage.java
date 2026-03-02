package com.wangzixian.usedcar.module.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@TableName("sys_chat_message")
@Table(name="sys_chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;
    private Long receiverId;
    private String content;
    private Long carId;
    
    // 👇 新增消息类型: 1-文本, 2-图片, 3-视频, 4-文件
    private Integer type;

    private Integer isRead;
    private LocalDateTime createTime;
}