package com.wangzixian.usedcar.module.car.service;

import com.wangzixian.usedcar.module.car.component.AiValuationClient;
import com.wangzixian.usedcar.module.car.entity.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiValuationService {

    @Autowired
    private AiValuationClient aiClient;

    public String generateValuationReport(Car car) {
        String systemPrompt = """
                你是一名拥有15年经验的中国二手车高级评估师。
                请根据用户提供的二手车详细参数，给出一份专业的文字版《二手车智能估值报告》。
                报告需包含以下三部分：
                1. 【核心亮点分析】：基于车型、年份、排量等给出该车的核心卖点。
                2. 【市场行情预估】：给出一个合理的二手车市场售卖价格区间（单位：万元），并简述理由。
                3. 【购买/售卖建议】：针对该车型的保值率、维修保养成本给出建议。
                注意：排版请清晰，直接输出纯文本报告，语气要客观、专业。
                """;

        String userMessage = String.format(
                "需评估车辆信息：\n品牌型号：%s %s\n首次上牌年份：%d年\n当前行驶里程：%.2f万公里\n过户次数：%d次\n排量/变速箱：%s %s\n车况等级：%s级\n目前卖家心理预期报价：%.2f万元",
                car.getBrand(), car.getModel(), 
                car.getBuyYear(), car.getMileage(), car.getTransferCount(),
                car.getDisplacement(), car.getGearbox(), 
                car.getConditionGrade(), car.getPrice()
        );

        return aiClient.generateReport(systemPrompt, userMessage);
    }
}