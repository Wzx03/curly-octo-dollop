package com.wangzixian.usedcar.common.aspect;

import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.wangzixian.usedcar.common.annotation.Log;
import com.wangzixian.usedcar.module.admin.entity.OperLog;
import com.wangzixian.usedcar.module.admin.mapper.OperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperLogMapper operLogMapper;

    /**
     * 处理完请求后执行
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 1. 获取 Request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 2. 创建日志对象
            OperLog operLog = new OperLog();
            operLog.setStatus(0);
            operLog.setOperIp(request.getRemoteAddr());
            operLog.setOperUrl(request.getRequestURI());
            operLog.setRequestMethod(request.getMethod());
            operLog.setMethod(joinPoint.getSignature().getName());
            operLog.setOperTime(LocalDateTime.now());

            // 3. 解析注解参数
            if (controllerLog != null) {
                operLog.setTitle(controllerLog.title());
                operLog.setBusinessType(controllerLog.businessType());
            }

            // 4. 获取当前用户 (从 Token)
            String token = request.getHeader("token");
            if (token != null) {
                try {
                    String username = JWTUtil.parseToken(token).getPayload("username").toString();
                    operLog.setOperName(username);
                } catch (Exception ignored) {}
            }

            // 5. 异常处理
            if (e != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(e.getMessage());
            }

            // 6. 请求参数 (简化处理，只取第一个参数)
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String params = JSONUtil.toJsonStr(args[0]);
                    operLog.setOperParam(params.length() > 2000 ? params.substring(0, 2000) : params);
                } catch (Exception ignored) {}
            }

            // 7. 返回结果
            if (jsonResult != null) {
                String json = JSONUtil.toJsonStr(jsonResult);
                operLog.setJsonResult(json.length() > 2000 ? json.substring(0, 2000) : json);
            }

            // 8. 保存数据库
            operLogMapper.insert(operLog);

        } catch (Exception exp) {
            // 记录日志失败不要影响业务
            exp.printStackTrace();
        }
    }
}