package com.wangzixian.usedcar.module.ai.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AiValuationClient {

    @Value("${ai.gateway.url:http://localhost:18789/v1/chat/completions}")
    private String gatewayUrl;

    @Value("${ai.gateway.token:123}")
    private String gatewayToken;

    @Value("${ai.gateway.model:aliyun/qwen-plus}")
    private String model;

    // 原有的估价单轮生成方法保持不变
    public String generateReport(String systemPrompt, String userPrompt) {
        JSONObject requestJson = JSONUtil.createObj()
                .set("model", model)
                .set("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ));
        return executeAiRequest(requestJson);
    }

    // 新增方法：支持多轮上下文聊天的接口调用
    public String chatWithHistory(List<Map<String, String>> messages) {
        JSONObject requestJson = JSONUtil.createObj()
                .set("model", model)
                .set("messages", messages);
        return executeAiRequest(requestJson);
    }

    // 抽离公共的网络请求逻辑
    private String executeAiRequest(JSONObject requestJson) {
        try {
            String responseStr = HttpRequest.post(gatewayUrl)
                    .header("Authorization", "Bearer " + gatewayToken)
                    .body(requestJson.toString())
                    .execute()
                    .body();

            if (!JSONUtil.isTypeJSON(responseStr)) {
                return "网关配置异常，原始回复为：" + responseStr;
            }

            JSONObject resultObj = JSONUtil.parseObj(responseStr);
            return resultObj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");
        } catch (Exception e) {
            return "AI调用发生异常：" + e.getMessage();
        }
    }
}