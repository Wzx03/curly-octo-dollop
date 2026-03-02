package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class MileageStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        int age = LocalDateTime.now().getYear() - car.getBuyYear();
        if (age < 0) age = 0;

        // 标准里程：每年 1.5 万公里
        double standardMileage = age * 1.5;
        double factor = 1.0;

        if (car.getMileage() > standardMileage) {
            // 跑多了，每多1万公里扣2%
            factor = 1.0 - ((car.getMileage() - standardMileage) / 10.0 * 0.02);
        } else {
            // 跑少了，每少1万公里加1%
            factor = 1.0 + ((standardMileage - car.getMileage()) / 10.0 * 0.01);
        }

        // 兜底范围：0.5 ~ 1.1
        return BigDecimal.valueOf(Math.max(0.5, Math.min(1.1, factor)));
    }
}