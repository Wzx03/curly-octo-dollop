package com.wangzixian.usedcar.module.car.strategy;

import com.wangzixian.usedcar.module.car.entity.Car;
import java.util.Map;

/**
 * 推荐策略接口
 * 用于计算某辆车与用户画像的匹配度得分
 */
public interface RecommendStrategy {
    /**
     * 计算得分
     * @param car 候选车辆
     * @param userProfile 用户画像 (包含偏好品牌、偏好价格区间等)
     * @return 匹配分数 (0 ~ 100)
     */
    double score(Car car, Map<String, Object> userProfile);
}