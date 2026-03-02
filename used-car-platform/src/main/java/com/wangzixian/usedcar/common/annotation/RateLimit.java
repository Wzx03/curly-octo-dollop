package com.wangzixian.usedcar.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流Key的前缀
     */
    String key() default "limit:";

    /**
     * 时间窗口（秒）
     */
    int time() default 1;

    /**
     * 允许请求的次数
     */
    int count() default 1;
}