//package com.wangzixian.usedcar.module.chat.server;
//
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import cn.hutool.jwt.JWTUtil;
//import com.wangzixian.usedcar.common.JwtUtils;
//import com.wangzixian.usedcar.module.ai.service.Aichatservice;
//import com.wangzixian.usedcar.module.chat.entity.ChatMessage;
//import com.wangzixian.usedcar.module.chat.mapper.ChatMessageMapper;
//import com.wangzixian.usedcar.module.user.entity.User;
//import com.wangzixian.usedcar.module.user.mapper.UserMapper;
//import jakarta.websocket.*;
//import jakarta.websocket.server.PathParam;
//import jakarta.websocket.server.ServerEndpoint;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//@Slf4j
//@Component
//@ServerEndpoint("/ws/chat/{token}")
//public class ChatServer {
//
//    private static final ConcurrentHashMap<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();
//    private static final ConcurrentHashMap<Long, Integer> USER_ROLES = new ConcurrentHashMap<>();
//
//    private static ChatMessageMapper chatMessageMapper;
//    private static UserMapper userMapper;
//
//    private static Aichatservice aichatservice;
//
//    @Autowired
//    public void setAichatservice(Aichatservice aichatservice) {
//        ChatServer.aichatservice = aichatservice;
//    }
//
//    @Autowired
//    public void setChatMessageMapper(ChatMessageMapper chatMessageMapper) {
//        ChatServer.chatMessageMapper = chatMessageMapper;
//    }
//
//    @Autowired
//    public void setUserMapper(UserMapper userMapper) {
//        ChatServer.userMapper = userMapper;
//    }
//
//    private Long userId;
//
//    @OnOpen
//    public void onOpen(Session session, @PathParam("token") String token) {
//        try {
//            this.userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
//
//            if (this.userId == null) {
//                session.close();
//                return;
//            }
//            ONLINE_USERS.put(this.userId, session);
//            User user = userMapper.selectById(this.userId);
//            if(user != null){
//                USER_ROLES.put(this.userId, user.getRole());
//            }
//            log.info("用户 {} 上线，当前在线人数: {}", this.userId, ONLINE_USERS.size());
//        } catch (Exception e) {
//            log.error("WebSocket 建立连接失败", e);
//        }
//    }
//
//    @OnClose
//    public void onClose(Session session) {
//        if (this.userId != null) {
//            ONLINE_USERS.remove(this.userId);
//            USER_ROLES.remove(this.userId);
//            log.info("用户 {} 下线，当前在线人数: {}", this.userId, ONLINE_USERS.size());
//        }
//    }
//
//    @OnMessage
//    public void onMessage(String message, Session session) {
//        try {
//            ChatMessage chatMsg = JSONUtil.toBean(message, ChatMessage.class);
//            Long receiverId = chatMsg.getReceiverId();
//            String content = chatMsg.getContent();
//
//            if (this.userId.equals(receiverId)) {
//                sendErrorMessage(session, "不能给自己发消息");
//                return;
//            }
//
//            // ================= 新增：🤖 拦截 AI 消息专用逻辑 =================
//            // 🚨 核心修复：根据你的 AiChatServiceImpl，AI 的专属 ID 应该是 0L
//            if (receiverId == 0L) {
//                new Thread(() -> {
//                    try {
//                        // 防止大模型网关无响应导致无限挂起，设置 45 秒硬超时
//                        // 注意：此时不要在外层再做任何 chatMessageMapper.insert，因为你的 aichatservice 内部已经处理了入库！
//                        String aiReply = CompletableFuture.supplyAsync(() -> {
//                            return aichatservice.processUserMessage(this.userId, content);
//                        }).get(45, TimeUnit.SECONDS);
//
//                        // 构建 AI 发送给用户的 WebSocket 回包
//                        ChatMessage replyMsg = new ChatMessage();
//                        replyMsg.setSenderId(0L); // AI 是 0L
//                        replyMsg.setReceiverId(this.userId);
//                        replyMsg.setContent(aiReply);
//                        replyMsg.setType(10); // 与你的 AI_CHAT_TYPE 保持一致
//                        replyMsg.setCreateTime(LocalDateTime.now());
//
//                        // 组装 JSON 推送给前端
//                        JSONObject pushMsg = new JSONObject();
//                        pushMsg.set("type", "chat");
//                        pushMsg.set("data", replyMsg);
//
//                        if (session.isOpen()) {
//                            session.getAsyncRemote().sendText(pushMsg.toString());
//                        }
//                    } catch (TimeoutException te) {
//                        log.error("AI 接口请求超时（超过45秒）");
//                        sendErrorMessage(session, "AI 接口网络响应较慢，请稍后再试");
//                    } catch (Throwable e) {
//                        log.error("AI 处理发生致命错误", e);
//                        sendErrorMessage(session, "AI 客服暂时离线或接口请求异常");
//                    }
//                }).start();
//
//                // 处理完 AI 逻辑后直接 return，跳过下方的入库和推送逻辑
//                return;
//            }
//            // ===============================================================
//
//            // 以下为普通用户的 P2P 实时推送逻辑
//            chatMsg.setSenderId(this.userId);
//            chatMsg.setCreateTime(LocalDateTime.now());
//            // 只有普通聊天才在此处入库
//            chatMessageMapper.insert(chatMsg);
//
//            Session receiverSession = ONLINE_USERS.get(receiverId);
//            if (receiverSession != null && receiverSession.isOpen()) {
//                JSONObject pushMsg = new JSONObject();
//                pushMsg.set("type", "chat");
//                pushMsg.set("data", chatMsg);
//                receiverSession.getAsyncRemote().sendText(pushMsg.toString());
//            }
//
//        } catch (Throwable e) {
//            log.error("消息处理失败", e);
//            sendErrorMessage(session, "消息发送失败");
//        }
//    }
//
//    @OnError
//    public void onError(Session session, Throwable error) {
//        log.error("WebSocket 错误", error);
//    }
//
//    private void sendErrorMessage(Session session, String msg) {
//        try {
//            JSONObject json = new JSONObject();
//            json.set("type", "error");
//            json.set("message", msg);
//            if (session.isOpen()) {
//                session.getAsyncRemote().sendText(json.toString());
//            }
//        } catch (Exception e) {
//            log.error("发送错误消息失败", e);
//        }
//    }
//
//    /**
//     * 👇 给所有在线管理员发送系统通知
//     */
//    public static void sendInfoToAdmins(String message) {
//        JSONObject json = new JSONObject();
//        json.set("type", "notice");
//        json.set("content", message);
//
//        USER_ROLES.forEach((uid, role) -> {
//            if (role == 0) {
//                Session session = ONLINE_USERS.get(uid);
//                if (session != null && session.isOpen()) {
//                    try {
//                        session.getAsyncRemote().sendText(json.toString());
//                    } catch (Exception e) {
//                        log.error("发送管理员通知失败", e);
//                    }
//                }
//            }
//        });
//    }
//}


package com.wangzixian.usedcar.module.chat.server;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.wangzixian.usedcar.common.JwtUtils;
import com.wangzixian.usedcar.module.ai.service.Aichatservice;
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
import java.util.concurrent.*;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{token}")
public class ChatServer {

    // 在线用户会话池
    private static final ConcurrentHashMap<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> USER_ROLES = new ConcurrentHashMap<>();

    // 🚀 优化 1：定义 AI 专属的请求线程池，防止高并发下 new Thread 导致资源耗尽
    private static final ExecutorService AI_EXECUTOR = Executors.newFixedThreadPool(15);

    private static ChatMessageMapper chatMessageMapper;
    private static UserMapper userMapper;
    private static Aichatservice aichatservice;

    @Autowired
    public void setAichatservice(Aichatservice aichatservice) {
        ChatServer.aichatservice = aichatservice;
    }

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
            this.userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());

            if (this.userId == null) {
                session.close();
                return;
            }
            ONLINE_USERS.put(this.userId, session);
            User user = userMapper.selectById(this.userId);
            if(user != null){
                USER_ROLES.put(this.userId, user.getRole());
            }
            log.info("用户 {} 上线，当前在线人数: {}", this.userId, ONLINE_USERS.size());
        } catch (Exception e) {
            log.error("WebSocket 建立连接失败", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (this.userId != null) {
            ONLINE_USERS.remove(this.userId);
            USER_ROLES.remove(this.userId);
            log.info("用户 {} 下线，当前在线人数: {}", this.userId, ONLINE_USERS.size());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ChatMessage chatMsg = JSONUtil.toBean(message, ChatMessage.class);
            Long receiverId = chatMsg.getReceiverId();
            String content = chatMsg.getContent();

            // 防御：禁止自发自收
            if (this.userId.equals(receiverId)) {
                sendErrorMessage(session, "不能给自己发消息");
                return;
            }
            if (receiverId == 0L) {
                handleAiMessage(session, content);
                return;
            }
            // ================= 普通用户的 P2P 实时推送逻辑 =================
            chatMsg.setSenderId(this.userId);
            chatMsg.setCreateTime(LocalDateTime.now());
            chatMessageMapper.insert(chatMsg);
            Session receiverSession = ONLINE_USERS.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                JSONObject pushMsg = new JSONObject();
                pushMsg.set("type", "chat");
                pushMsg.set("data", chatMsg);
                receiverSession.getAsyncRemote().sendText(pushMsg.toString());
            }
        } catch (Throwable e) {
            log.error("消息处理失败", e);
            sendErrorMessage(session, "消息发送失败");
        }
    }
    /**
     * 🚀 优化 2 & 3：抽离独立方法，使用 CompletableFuture 实现纯异步非阻塞的大模型调用
     */
    private void handleAiMessage(Session session, String content) {
        CompletableFuture.supplyAsync(() -> aichatservice.processUserMessage(this.userId, content), AI_EXECUTOR)
                .orTimeout(45, TimeUnit.SECONDS) // 设置 45 秒硬超时，到点自动抛出 TimeoutException
                .whenComplete((aiReply, ex) -> {
                    // 异常回调处理
                    if (ex != null) {
                        if (ex instanceof TimeoutException || ex.getCause() instanceof TimeoutException) {
                            log.warn("用户 {} 的 AI 接口请求超时（超过45秒）", this.userId);
                            sendErrorMessage(session, "AI 接口网络响应较慢，请稍后再试");
                        } else {
                            log.error("AI 处理发生致命错误", ex);
                            sendErrorMessage(session, "AI 客服暂时离线或接口请求异常");
                        }
                        return; // 异常后直接结束
                    }

                    // 正常成功回调处理
                    try {
                        // 构建 AI 回包结构
                        ChatMessage replyMsg = new ChatMessage();
                        replyMsg.setSenderId(0L);
                        replyMsg.setReceiverId(this.userId);
                        replyMsg.setContent(aiReply);
                        replyMsg.setType(10); // 与 AI_CHAT_TYPE 一致
                        replyMsg.setCreateTime(LocalDateTime.now());

                        // 组装 JSON 推送给前端
                        JSONObject pushMsg = new JSONObject();
                        pushMsg.set("type", "chat");
                        pushMsg.set("data", replyMsg);

                        if (session.isOpen()) {
                            session.getAsyncRemote().sendText(pushMsg.toString());
                        }
                    } catch (Exception e) {
                        log.error("推送 AI 消息到前端失败", e);
                    }
                });
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
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(json.toString());
            }
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 👇 给所有在线管理员发送系统通知
     */
    public static void sendInfoToAdmins(String message) {
        JSONObject json = new JSONObject();
        json.set("type", "notice");
        json.set("content", message);

        USER_ROLES.forEach((uid, role) -> {
            if (role == 0) {
                Session session = ONLINE_USERS.get(uid);
                if (session != null && session.isOpen()) {
                    try {
                        session.getAsyncRemote().sendText(json.toString());
                    } catch (Exception e) {
                        log.error("发送管理员通知失败", e);
                    }
                }
            }
        });
    }
}