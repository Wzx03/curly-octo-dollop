package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmissionStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        String emission = car.getEmissionStandard();
        if (emission == null) return BigDecimal.ONE;

        if (emission.contains("VI") || emission.contains("六")) {
            return BigDecimal.valueOf(1.05); // 国6保值
        } else if (emission.contains("V") || emission.contains("五")) {
            return BigDecimal.valueOf(1.00); // 国5正常
        } else {
            return BigDecimal.valueOf(0.70); // 国4及以下，骨折价
        }
    }
}