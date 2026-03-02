package com.wangzixian.usedcar.module.car.strategy;

import com.wangzixian.usedcar.module.car.entity.Car;
import java.math.BigDecimal;

/**
 * 估价策略接口
 * 每一个实现类代表一个影响价格的因子（如：品牌、里程、颜色）
 */
public interface ValuationStrategy {
    /**
     * 计算该因子对价格的影响系数
     * @param car 车辆信息
     * @return 影响系数 (例如 0.95 代表打95折)
     */
    BigDecimal calculateFactor(Car car);
}