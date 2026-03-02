package com.wangzixian.usedcar.module.chat.controller;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.chat.entity.ChatMessage;
import com.wangzixian.usedcar.module.chat.mapper.ChatMessageMapper;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class MessageController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    private Long getUserId(String token) {
        return Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
    }

    /**
     * 分页获取历史记录 (大厂标准：倒序查，正序排)
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> history(
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("token") String token) {
        
        Long myId = getUserId(token);

        Page<ChatMessage> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.and(w -> w
                .eq(ChatMessage::getSenderId, myId).eq(ChatMessage::getReceiverId, targetId)
                .or()
                .eq(ChatMessage::getSenderId, targetId).eq(ChatMessage::getReceiverId, myId)
        );
        
        wrapper.orderByDesc(ChatMessage::getCreateTime);

        Page<ChatMessage> resultPage = chatMessageMapper.selectPage(pageParam, wrapper);
        List<ChatMessage> records = resultPage.getRecords();

        Collections.reverse(records);

        return Result.success(records);
    }

    /**
     * 标记已读
     */
    @PostMapping("/read")
    public Result<String> markRead(@RequestParam Long targetId, @RequestHeader("token") String token) {
        Long myId = getUserId(token);

        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatMessage::getSenderId, targetId)
                     .eq(ChatMessage::getReceiverId, myId)
                     .eq(ChatMessage::getIsRead, 0)
                     .set(ChatMessage::getIsRead, 1);

        chatMessageMapper.update(null, updateWrapper);
        return Result.success("已读");
    }

    /**
     * 获取我的最近联系人列表 (含未读数)
     */
    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> sessions(@RequestHeader("token") String token) {
        Long myId = getUserId(token);

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSenderId, myId).or().eq(ChatMessage::getReceiverId, myId);
        wrapper.orderByDesc(ChatMessage::getCreateTime);
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);

        Set<Long> contactIds = new LinkedHashSet<>();
        for (ChatMessage msg : messages) {
            if (msg.getSenderId().equals(myId)) {
                contactIds.add(msg.getReceiverId());
            } else {
                contactIds.add(msg.getSenderId());
            }
        }

        if (contactIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<User> users = userMapper.selectBatchIds(contactIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Map<String, Object>> sessionList = new ArrayList<>();
        for (Long contactId : contactIds) {
            User contact = userMap.get(contactId);
            if (contact == null) continue;

            ChatMessage lastMsg = messages.stream()
                    .filter(m -> (m.getSenderId().equals(myId) && m.getReceiverId().equals(contactId)) ||
                                 (m.getSenderId().equals(contactId) && m.getReceiverId().equals(myId)))
                    .findFirst()
                    .orElse(null);
            
            // 计算未读数
            long unreadCount = messages.stream()
                    .filter(m -> m.getSenderId().equals(contactId) && m.getReceiverId().equals(myId) && m.getIsRead() == 0)
                    .count();

            Map<String, Object> session = new HashMap<>();
            session.put("userId", contact.getId());
            session.put("nickname", contact.getNickname() != null ? contact.getNickname() : contact.getUsername());
            session.put("avatar", contact.getAvatar());
            session.put("lastMsg", lastMsg != null ? lastMsg.getContent() : "");
            session.put("lastMsgType", lastMsg != null ? lastMsg.getType() : 1); // 👈 补上类型
            session.put("lastTime", lastMsg != null ? lastMsg.getCreateTime() : null);
            session.put("unread", unreadCount); // 👈 增加未读数
            
            sessionList.add(session);
        }

        return Result.success(sessionList);
    }
}