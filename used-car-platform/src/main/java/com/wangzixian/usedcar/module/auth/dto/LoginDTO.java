package com.wangzixian.usedcar.module.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank; // 如果是 Spring Boot 2.x，使用 javax.validation

@Data
public class LoginDTO {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe;
}