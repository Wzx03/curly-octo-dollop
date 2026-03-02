package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EnergyStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        String energy = car.getEnergyType();
        if (energy == null) return BigDecimal.ONE;

        // 新能源/混动：电池损耗大，贬值快
        if (energy.contains("新能源") || energy.contains("混合") || energy.contains("电")) {
            return BigDecimal.valueOf(0.90);
        }
        
        // 燃油车：正常
        return BigDecimal.ONE;
    }
}