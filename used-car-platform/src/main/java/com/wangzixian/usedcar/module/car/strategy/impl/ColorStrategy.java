package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ColorStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        String color = car.getColor() != null ? car.getColor() : "";
        if (color.contains("粉") || color.contains("绿") || color.contains("紫")) {
            return BigDecimal.valueOf(0.95); // 冷门色打95折
        }
        return BigDecimal.ONE;
    }
}