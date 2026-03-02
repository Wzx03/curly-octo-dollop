package com.wangzixian.usedcar.common;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;

import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    // 密钥 (正式上线要写复杂点)
    private static final byte[] KEY = "WangZiXianUsedCarPlatformSecretKey2026".getBytes();

    /**
     * 1. 生成 Token (登录时用)
     */
    public static String createToken(Long userId, String username, Integer role) { // 👈 增加 role 参数
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", userId);
        payload.put("username", username);
        payload.put("role", role); // 👈 存入 role
        // 设置过期时间：当前时间 + 24小时 (毫秒)
        payload.put("expire_time", System.currentTimeMillis() + 1000 * 60 * 60 * 24);

        return JWTUtil.createToken(payload, JWTSignerUtil.hs256(KEY));
    }

    /**
     * 2. 校验 Token (拦截器里用)
     */
    public static boolean validateToken(String token) {
        try {
            // 步骤A: 验证签名是否被篡改
            boolean verify = JWTUtil.verify(token, JWTSignerUtil.hs256(KEY));
            if (!verify) {
                return false;
            }

            // 步骤B: 验证是否过期
            JWT jwt = JWTUtil.parseToken(token);
            long expireTime = Long.parseLong(jwt.getPayload("expire_time").toString());
            long now = System.currentTimeMillis();

            // 如果 当前时间 > 过期时间，说明过期了
            if (now > expireTime) {
                return false;
            }

            return true; // 验证通过
        } catch (Exception e) {
            return false; // 只要出异常，统统算验证失败
        }
    }
}