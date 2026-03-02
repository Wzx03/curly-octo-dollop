package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PriceMatchStrategy implements RecommendStrategy {

    @Override
    public double score(Car car, Map<String, Object> userProfile) {
        Double avgPrice = (Double) userProfile.get("avgPrice");
        if (avgPrice == null || car.getPrice() == null) return 0;

        double carPrice = car.getPrice().doubleValue();
        
        // 计算价格偏差率：|车价 - 心理价位| / 心理价位
        double diffRate = Math.abs(carPrice - avgPrice) / avgPrice;

        // 如果偏差在 20% 以内，给高分
        if (diffRate <= 0.2) {
            return 40.0; // 满分40
        } else if (diffRate <= 0.5) {
            return 20.0; // 偏差较大，给一半分
        }
        
        return 0; // 太贵或太便宜，不推荐
    }
}