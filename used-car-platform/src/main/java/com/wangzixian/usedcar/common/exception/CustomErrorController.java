package com.wangzixian.usedcar.common.exception;

import com.wangzixian.usedcar.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Result<String> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        }
        
        if (statusCode == 404) {
            return Result.error("接口不存在 (404)");
        } else if (statusCode == 401) {
            return Result.error("未授权 (401)");
        } else if (statusCode == 403) {
            return Result.error("禁止访问 (403)");
        }
        
        return Result.error("系统内部错误 (" + statusCode + ")");
    }
}