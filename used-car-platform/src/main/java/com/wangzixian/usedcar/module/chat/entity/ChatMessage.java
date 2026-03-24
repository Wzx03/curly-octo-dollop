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

    // 👇 扩充消息类型: 1-文本, 2-图片, 3-视频, 4-文件, 10-AI客服问答, 11-AI估价卡片
    private Integer type;

    // 👇 新增字段：用于对接大模型的角色隔离
    // 枚举值: user(用户提问), assistant(AI回复), system(系统提示词)
    @Column(name = "role", length = 20)
    private String role;

    private Integer isRead;
    private LocalDateTime createTime;
}