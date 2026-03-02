package com.wangzixian.usedcar.common.annotation;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能 (0-其他, 1-新增, 2-修改, 3-删除)
     */
    int businessType() default 0;
}