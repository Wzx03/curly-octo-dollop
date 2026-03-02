package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MaintenanceStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        String type = car.getMaintenanceType();
        if (type == null) return BigDecimal.ONE;

        if ("4S".equalsIgnoreCase(type)) {
            return BigDecimal.valueOf(1.02); // 全程4S店，加价2%
        } else {
            return BigDecimal.valueOf(0.98); // 路边摊，微跌
        }
    }
}