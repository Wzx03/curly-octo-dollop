package com.wangzixian.usedcar.module.chat.server;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.wangzixian.usedcar.common.JwtUtils;
import com.wangzixian.usedcar.module.chat.entity.ChatMessage;
import com.wangzixian.usedcar.module.chat.mapper.ChatMessageMapper;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{token}")
public class ChatServer {

    private static final ConcurrentHashMap<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> USER_ROLES = new ConcurrentHashMap<>(); // 👈 存储角色
    private static ChatMessageMapper chatMessageMapper;
    private static UserMapper userMapper;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper chatMessageMapper) {
        ChatServer.chatMessageMapper = chatMessageMapper;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        ChatServer.userMapper = userMapper;
    }

    private Long userId;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            if (!JwtUtils.validateToken(token)) {
                session.close();
                return;
            }
            // 解析 ID 和 Role
            cn.hutool.jwt.JWT jwt = JWTUtil.parseToken(token);
            this.userId = Long.valueOf(jwt.getPayload("id").toString());
            Integer role = Integer.valueOf(jwt.getPayload("role").toString());
            
            ONLINE_USERS.put(this.userId, session);
            USER_ROLES.put(this.userId, role);
        } catch (Exception e) {
            try { session.close(); } catch (Exception ignored) {}
        }
    }

    @OnClose
    public void onClose() {
        if (this.userId != null) {
            ONLINE_USERS.remove(this.userId);
            USER_ROLES.remove(this.userId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject json = JSONUtil.parseObj(message);
            Long receiverId = json.getLong("receiverId");
            String content = json.getStr("content");
            Long carId = json.getLong("carId");
            Integer type = json.getInt("type");
            if (type == null) type = 1;

            // 1. 禁止给自己发消息
            if (this.userId.equals(receiverId)) {
                sendErrorMessage(session, "不能给自己发消息");
                return;
            }

            // 2. 验证发送者是否存在
            User sender = userMapper.selectById(this.userId);
            if (sender == null) {
                sendErrorMessage(session, "发送者账号不存在");
                session.close();
                return;
            }

            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setSenderId(this.userId);
            chatMsg.setReceiverId(receiverId);
            chatMsg.setContent(content);
            chatMsg.setCarId(carId);
            chatMsg.setType(type);
            chatMsg.setIsRead(0);
            chatMsg.setCreateTime(LocalDateTime.now());
            chatMessageMapper.insert(chatMsg);

            Session receiverSession = ONLINE_USERS.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                JSONObject pushMsg = new JSONObject();
                pushMsg.set("type", "chat");
                pushMsg.set("data", chatMsg);
                receiverSession.getBasicRemote().sendText(pushMsg.toString());
            }

        } catch (Exception e) {
            log.error("消息处理失败", e);
            sendErrorMessage(session, "消息发送失败");
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误", error);
    }

    private void sendErrorMessage(Session session, String msg) {
        try {
            JSONObject json = new JSONObject();
            json.set("type", "error");
            json.set("message", msg);
            session.getBasicRemote().sendText(json.toString());
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 👇 给所有在线管理员发送系统通知
     */
    public static void sendInfoToAdmins(String message) {
        JSONObject json = new JSONObject();
        json.set("type", "notice"); // 消息类型：通知
        json.set("content", message);
        
        USER_ROLES.forEach((uid, role) -> {
            if (role == 0) { // 0 是管理员
                Session session = ONLINE_USERS.get(uid);
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}