package com.wangzixian.usedcar.common.exception;

import com.wangzixian.usedcar.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 拦截所有 Controller 抛出的异常，转为统一的 Result 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截所有 RuntimeException (包括我们自己抛出的 "抢购失败"、"限流" 等)
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage());
        // 直接把报错信息返给前端，状态码设为 500
        return Result.error(e.getMessage());
    }

    /**
     * 拦截所有未知的 Exception
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}