package com.wangzixian.usedcar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("二手车交易平台 API 文档")
                        .version("1.0")
                        .description("基于 Spring Boot 3 + Vue 3 的全栈项目")
                        .contact(new Contact().name("王自贤").email("wangzixian@example.com")));
    }
}