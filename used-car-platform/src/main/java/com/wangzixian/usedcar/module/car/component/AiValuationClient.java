package com.wangzixian.usedcar.module.car.component;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class AiValuationClient {

    // 赋予默认值，防止配置文件读取失败导致启动报错
    @Value("${ai.gateway.url:http://localhost:18789/v1/chat/completions}")
    private String gatewayUrl;

    @Value("${ai.gateway.token:123}")
    private String gatewayToken;

    @Value("${ai.gateway.model:aliyun/qwen-plus}")
    private String model;

    public String generateReport(String systemPrompt, String userPrompt) {
        // 1. 将变量名从 body 改为 requestJson，避免跟 Hutool 的方法名冲突
        JSONObject requestJson = JSONUtil.createObj()
                .set("model", model)
                .set("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ));

        // ... 前面代码保持不变 ...
        try {
            String responseStr = HttpRequest.post(gatewayUrl)
                    .header("Authorization", "Bearer " + gatewayToken)
                    .body(requestJson.toString())
                    .execute()
                    .body();

            System.out.println("AI原始返回内容：" + responseStr);

            // 👇 关键：先判断返回的是不是 JSON，如果不是，直接把原始内容显示出来
            if (!JSONUtil.isTypeJSON(responseStr)) {
                return "网关配置异常，原始回复为：" + responseStr;
            }

            JSONObject resultObj = JSONUtil.parseObj(responseStr);
            return resultObj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");
        } catch (Exception e) {
            System.out.println("最终请求地址：" + gatewayUrl);
            return "调用发生异常：" + e.getMessage();
        }

    }
}