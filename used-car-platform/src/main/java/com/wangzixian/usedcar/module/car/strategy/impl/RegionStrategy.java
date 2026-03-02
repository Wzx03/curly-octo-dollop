package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class RegionStrategy implements ValuationStrategy {

    private static final List<String> TIER_1_CITIES = Arrays.asList("北京", "上海", "广州", "深圳", "杭州");

    @Override
    public BigDecimal calculateFactor(Car car) {
        String city = car.getCity();
        if (city == null) return BigDecimal.ONE;

        for (String tier1 : TIER_1_CITIES) {
            if (city.contains(tier1)) {
                return BigDecimal.valueOf(1.05); // 一线城市，加价5%
            }
        }
        return BigDecimal.ONE;
    }
}