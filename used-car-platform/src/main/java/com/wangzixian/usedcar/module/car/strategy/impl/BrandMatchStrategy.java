package com.wangzixian.usedcar.module.car.strategy.impl;

import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.strategy.RecommendStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BrandMatchStrategy implements RecommendStrategy {

    @Override
    public double score(Car car, Map<String, Object> userProfile) {
        // 从画像中获取用户最喜欢的品牌
        String favBrand = (String) userProfile.get("favBrand");
        
        if (favBrand == null || car.getBrand() == null) {
            return 0;
        }

        // 模糊匹配：如果用户喜欢 "宝马"，而这辆车是 "宝马3系"，加 50 分
        if (car.getBrand().toLowerCase().contains(favBrand.toLowerCase())) {
            return 50.0;
        }
        
        return 0;
    }
}