package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ConditionStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        String grade = car.getConditionGrade();
        if (grade == null) return BigDecimal.ONE; // 默认不影响

        switch (grade.toUpperCase()) {
            case "A": return BigDecimal.valueOf(1.05); // 精品车，加价5%
            case "B": return BigDecimal.valueOf(1.00); // 良好，基准
            case "C": return BigDecimal.valueOf(0.90); // 一般，打9折
            case "D": return BigDecimal.valueOf(0.60); // 事故车，打6折
            default: return BigDecimal.ONE;
        }
    }
}