package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HotnessStrategy implements RecommendStrategy {

    @Override
    public double score(Car car, Map<String, Object> userProfile) {
        Integer views = car.getViews();
        if (views == null) views = 0;

        // 简单粗暴：每 10 次浏览加 1 分，封顶 20 分
        double score = views / 10.0;
        return Math.min(score, 20.0);
    }
}