package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ConfigStrategy implements ValuationStrategy {

    @Override
    public BigDecimal calculateFactor(Car car) {
        double factor = 1.0;

        // 1. 驱动方式
        String drive = car.getDriveMode();
        if (drive != null && drive.contains("四驱")) {
            factor += 0.05; // 四驱 +5%
        }

        // 2. 变速箱
        String gearbox = car.getGearbox();
        if (gearbox != null && gearbox.contains("自动")) {
            factor += 0.03; // 自动挡 +3%
        }

        // 3. 排量
        String disp = car.getDisplacement();
        if (disp != null) {
            if (disp.contains("3.0") || disp.contains("4.0") || disp.contains("5.0")) {
                factor += 0.10; // 大排量 +10%
            } else if (disp.contains("1.0") || disp.contains("0.8") || disp.contains("0.9")) {
                factor -= 0.05; // 小排量 -5%
            }
        }

        return BigDecimal.valueOf(factor);
    }
}