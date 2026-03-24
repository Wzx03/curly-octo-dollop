package com.wangzixian.usedcar.module.ai.controller;

import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.ai.service.Aichatservice;
import com.wangzixian.usedcar.module.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AichatController {

    private final Aichatservice aichatservice;

    /**
     * 获取当前用户的AI对话历史
     */
    @GetMapping("/history/{userId}")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable Long userId) {
        List<ChatMessage> history = aichatservice.getAiChatHistory(userId);
        return Result.success(history);
    }

    /**
     * 发送问题给AI客服
     */
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody Map<String, Object> params) {
        // 在实际开发中，userId 通常从 Token 拦截器中获取（如 JwtUtils.getUserId()）
        // 这里为了接口测试方便，暂用参数传递
        Long userId = Long.valueOf(params.get("userId").toString());
        String content = params.get("content").toString();

        String aiReply = aichatservice.processUserMessage(userId, content);
        return Result.success(aiReply);
    }
}