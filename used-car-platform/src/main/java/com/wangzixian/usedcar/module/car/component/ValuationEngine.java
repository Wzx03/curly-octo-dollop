package com.wangzixian.usedcar.module.car.component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.mapper.CarMapper;
import com.wangzixian.usedcar.module.car.strategy.ValuationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 混合估价引擎
 * 结合 "规则计算" 与 "市场比价"
 */
@Component
public class ValuationEngine {

    @Autowired
    private List<ValuationStrategy> strategies;

    @Autowired
    private CarMapper carMapper;

    public Double assess(Car car) {
        if (car.getOriginalPrice() == null) return 0.0;

        // --- 步骤A: 基础规则估价 (Base Price) ---
        BigDecimal finalFactor = BigDecimal.ONE;
        for (ValuationStrategy strategy : strategies) {
            BigDecimal factor = strategy.calculateFactor(car);
            finalFactor = finalFactor.multiply(factor);
        }
        
        // 基础估价 = 新车价 * 综合系数
        BigDecimal basePrice = BigDecimal.valueOf(car.getOriginalPrice()).multiply(finalFactor);

        // --- 步骤B: 车况微调 (Condition Adjustment) ---
        // 1-极品(1.05), 2-良好(1.0), 3-一般(0.9)
        double conditionFactor = 1.0;
        if (car.getConditionLevel() != null) {
            if (car.getConditionLevel() == 1) conditionFactor = 1.05;
            else if (car.getConditionLevel() == 3) conditionFactor = 0.90;
        }
        basePrice = basePrice.multiply(BigDecimal.valueOf(conditionFactor));

        // --- 步骤C: 市场比价 (Market Price) ---
        Double avgPrice = getMarketAvgPrice(car);

        // --- 步骤D: 混合计算 (Hybrid Calculation) ---
        BigDecimal finalPrice;
        if (avgPrice != null && avgPrice > 0) {
            // 存在市场价：Base * 0.4 + Market * 0.6
            BigDecimal marketPart = BigDecimal.valueOf(avgPrice).multiply(BigDecimal.valueOf(0.6));
            BigDecimal basePart = basePrice.multiply(BigDecimal.valueOf(0.4));
            finalPrice = basePart.add(marketPart);
        } else {
            // 无市场参考：直接用 Base Price
            finalPrice = basePrice;
        }

        // 兜底逻辑 (最低 5%)
        BigDecimal minPrice = BigDecimal.valueOf(car.getOriginalPrice() * 0.05);
        if (finalPrice.compareTo(minPrice) < 0) {
            finalPrice = minPrice;
        }

        return finalPrice.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 查询市场均价
     * 逻辑：同品牌 + 同型号(模糊) + 车龄(±2年) + 状态(1-在售)
     */
    private Double getMarketAvgPrice(Car car) {
        if (car.getBrand() == null || car.getModel() == null || car.getBuyYear() == null) {
            return null;
        }

        QueryWrapper<Car> query = new QueryWrapper<>();
        query.eq("status", 1); // 只看在售的
        query.eq("brand", car.getBrand());
        query.like("model", car.getModel()); // 型号模糊匹配
        // 车龄 ±2 年
        query.ge("buy_year", car.getBuyYear() - 2);
        query.le("buy_year", car.getBuyYear() + 2);
        
        // 排除自己 (如果是修改操作)
        if (car.getId() != null) {
            query.ne("id", car.getId());
        }

        List<Car> marketCars = carMapper.selectList(query);
        if (marketCars.isEmpty()) {
            return null;
        }

        // 计算平均价
        double total = marketCars.stream()
                .map(c -> c.getPrice() != null ? c.getPrice().doubleValue() : 0.0)
                .reduce(0.0, Double::sum);
        
        return total / marketCars.size();
    }
}