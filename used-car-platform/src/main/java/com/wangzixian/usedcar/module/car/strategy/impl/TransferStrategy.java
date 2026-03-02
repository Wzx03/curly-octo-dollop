package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        Integer count = car.getTransferCount();
        if (count == null) count = 0;

        if (count == 0) {
            return BigDecimal.valueOf(1.03); // 一手车，加价3%
        } else if (count == 1) {
            return BigDecimal.valueOf(0.98); // 过户一次，微跌
        } else if (count == 2) {
            return BigDecimal.valueOf(0.95);
        } else {
            return BigDecimal.valueOf(0.90); // 多次过户，打9折
        }
    }
}