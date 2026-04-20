package com.wangzixian.usedcar.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangzixian.usedcar.module.ai.client.AiValuationClient;
import com.wangzixian.usedcar.module.ai.service.Aichatservice;
import com.wangzixian.usedcar.module.chat.entity.ChatMessage;
import com.wangzixian.usedcar.module.chat.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements Aichatservice {

    private final ChatMessageMapper chatMessageMapper;
    private final AiValuationClient aiApiClient;

    private static final Long SYSTEM_AI_ID = 0L; // 系统AI的虚拟ID
    private static final Integer AI_CHAT_TYPE = 10; // 区分于普通P2P聊天

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public String processUserMessage(Long userId, String userContent) {
        // 1. 保存用户的提问记录
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSenderId(userId);
        userMsg.setReceiverId(SYSTEM_AI_ID);
        userMsg.setContent(userContent);
        userMsg.setRole("user");
        userMsg.setType(AI_CHAT_TYPE);
        userMsg.setIsRead(1);
        userMsg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(userMsg);
        // 2. 加载最近历史上下文（取最近10条防止Token超限）
        List<ChatMessage> historyList = getAiChatHistory(userId);
        // 3. 构建大模型标准请求格式
        List<Map<String, String>> messages = new ArrayList<>();
        // 压入系统提示词 (System Prompt)，巩固二手车客服人设
        messages.add(Map.of("role", "system", "content", "你是一名专业的二手车平台智能交易顾问。请解答用户关于购车流程、车辆参数的问题。如果用户询问代码或系统架构，请礼貌地拒绝。"));
        // 压入历史上下文
        for (ChatMessage msg : historyList) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        // 4. 发起 HTTP 调用
        String aiResponseText = aiApiClient.chatWithHistory(messages);
        // 5. 保存 AI 的回复记录
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderId(SYSTEM_AI_ID);
        aiMsg.setReceiverId(userId);
        aiMsg.setContent(aiResponseText);
        aiMsg.setRole("assistant");
        aiMsg.setType(AI_CHAT_TYPE);
        aiMsg.setIsRead(0);
        aiMsg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(aiMsg);
        return aiResponseText;
    }

    @Override
    public List<ChatMessage> getAiChatHistory(Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getType, AI_CHAT_TYPE)
                .and(w -> w.eq(ChatMessage::getSenderId, userId).or().eq(ChatMessage::getReceiverId, userId))
                .orderByDesc(ChatMessage::getCreateTime)
                .last("limit 10");

        List<ChatMessage> history = chatMessageMapper.selectList(wrapper);
        // 按时间正序排列以符合上下文习惯
        Collections.reverse(history);
        return history;
    }
}