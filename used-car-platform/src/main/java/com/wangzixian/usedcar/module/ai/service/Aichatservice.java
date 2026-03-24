package com.wangzixian.usedcar.module.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzixian.usedcar.module.chat.entity.ChatMessage;

import java.util.List;

public interface Aichatservice extends IService<ChatMessage> {

    /**
     * 处理用户的提问并调用AI返回结果
     */
    String processUserMessage(Long userId, String userContent);

    /**
     * 获取指定用户的AI聊天历史
     */
    List<ChatMessage> getAiChatHistory(Long userId);
}