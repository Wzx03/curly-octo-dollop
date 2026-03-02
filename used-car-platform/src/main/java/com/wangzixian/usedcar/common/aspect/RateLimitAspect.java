package com.wangzixian.usedcar.common.aspect;

import com.wangzixian.usedcar.common.RedisUtil;
import com.wangzixian.usedcar.common.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate; // 直接用 Template 操作更灵活

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 1. 获取 Request 对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 2. 获取 IP 地址 (简单版，实际可能需要处理 X-Forwarded-For)
        String ip = request.getRemoteAddr();

        // 3. 获取方法名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        // 4. 拼接 Redis Key: limit:createOrder:127.0.0.1
        String key = rateLimit.key() + methodName + ":" + ip;

        // 5. Redis 计数逻辑
        // increment 是原子操作，返回当前是第几次请求
        Long count = redisTemplate.opsForValue().increment(key);

        // 如果是第一次访问，设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, rateLimit.time(), TimeUnit.SECONDS);
        }

        // 6. 判断是否超限
        if (count != null && count > rateLimit.count()) {
            throw new RuntimeException("访问太频繁，请稍后再试！");
        }

        // 7. 放行
        return point.proceed();
    }
}