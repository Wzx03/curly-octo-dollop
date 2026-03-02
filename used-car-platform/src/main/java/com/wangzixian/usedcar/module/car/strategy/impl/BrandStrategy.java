package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class BrandStrategy implements ValuationStrategy {

    private static final Map<String, Double> BRAND_RATE = new HashMap<>();
    static {
        BRAND_RATE.put("toyota", 0.92);
        BRAND_RATE.put("honda", 0.91);
        BRAND_RATE.put("benz", 0.85);
        BRAND_RATE.put("bmw", 0.84);
        BRAND_RATE.put("audi", 0.83);
        BRAND_RATE.put("tesla", 0.80);
    }

    @Override
    public BigDecimal calculateFactor(Car car) {
        int age = LocalDateTime.now().getYear() - car.getBuyYear();
        if (age < 0) age = 0;

        double rate = 0.80; // 默认保值率
        String brand = car.getBrand() != null ? car.getBrand().toLowerCase() : "";
        
        for (Map.Entry<String, Double> entry : BRAND_RATE.entrySet()) {
            if (brand.contains(entry.getKey())) {
                rate = entry.getValue();
                break;
            }
        }

        // 核心公式：保值率 ^ 车龄
        return BigDecimal.valueOf(Math.pow(rate, age));
    }
}