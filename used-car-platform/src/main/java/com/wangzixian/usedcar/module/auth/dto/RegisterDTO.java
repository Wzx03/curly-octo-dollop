package com.wangzixian.usedcar.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
}