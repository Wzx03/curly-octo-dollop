package com.wangzixian.usedcar.common.interceptor;

import cn.hutool.json.JSONUtil;
import com.wangzixian.usedcar.common.JwtUtils;
import com.wangzixian.usedcar.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. 如果是 OPTIONS 请求，直接放行
        if("OPTIONS".equals(method)) {
            return true;
        }

        // 2. 公开接口放行 (车辆列表、车辆详情、文章列表、文章详情)
        // 注意：只放行 GET 请求，防止 POST/PUT/DELETE 被绕过
        if ("GET".equalsIgnoreCase(method)) {
            // 使用 startsWith 更加稳健，防止末尾斜杠等问题
            if (uri.startsWith("/api/car/list") || uri.matches("/api/car/\\d+")) {
                return true;
            }
            if (uri.startsWith("/api/content/article/list") || uri.matches("/api/content/article/\\d+")) {
                return true;
            }
        }

        // 3. 从请求头中获取 token
        String token = request.getHeader("token");

        // 4. 校验 Token 是否有效
        if (token != null && !token.isEmpty()) {
            try {
                // 调用 JwtUtils 验证
                if (JwtUtils.validateToken(token)) {
                    return true; // 验证通过，放行
                }
            } catch (Exception e) {
                // 验证失败
            }
        }

        // 5. 验证失败：返回 401
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");

        //只传一个参数
        String json = JSONUtil.toJsonStr(Result.error("请先登录！"));

        response.getWriter().write(json);

        return false; // 拦截
    }
}