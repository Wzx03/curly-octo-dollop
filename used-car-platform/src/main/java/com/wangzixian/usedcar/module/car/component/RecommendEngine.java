package com.wangzixian.usedcar.module.car.component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangzixian.usedcar.module.car.entity.BrowseLog;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.mapper.BrowseLogMapper;
import com.wangzixian.usedcar.module.car.service.CarService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecommendEngine {

    @Autowired
    private BrowseLogMapper browseLogMapper;

    @Autowired
    private CarService carService;

    public List<Car> recommend(Long userId) {
        // 1. 获取所有在售车辆 (候选集)
        List<Car> candidates = carService.list(new QueryWrapper<Car>().eq("status", 1));
        if (candidates.isEmpty()) return new ArrayList<>();

        // 2. 构建用户画像
        Map<String, Object> profile = buildUserProfile(userId);
        boolean isNewUser = profile.isEmpty();

        List<CarScore> scoredCars = new ArrayList<>();

        for (Car car : candidates) {
            // 过滤掉自己发布的
            if (car.getUserId().equals(userId)) continue;

            double score = 0.0;

            // --- 基础分：热度 + 新鲜度 ---
            // 热度分：浏览量 * 0.5
            score += (car.getViews() == null ? 0 : car.getViews()) * 0.5;
            
            // 新鲜度分：最近 7 天发布的 +20 分
            if (car.getCreateTime().isAfter(java.time.LocalDateTime.now().minusDays(7))) {
                score += 20;
            }

            // --- 个性化分 (仅老用户) ---
            if (!isNewUser) {
                // 1. 品牌匹配 (+50分)
                String favBrand = (String) profile.get("favBrand");
                if (favBrand != null && favBrand.equals(car.getBrand())) {
                    score += 50;
                }

                // 2. 价格匹配 (+30分)
                // 如果价格在用户偏好均价的 ±20% 范围内
                Double avgPrice = (Double) profile.get("avgPrice");
                if (avgPrice != null && car.getPrice() != null) {
                    double price = car.getPrice().doubleValue();
                    if (price >= avgPrice * 0.8 && price <= avgPrice * 1.2) {
                        score += 30;
                    }
                }
            }

            // --- 随机扰动 (0-5分) ---
            // 防止每次推荐顺序完全一样
            score += Math.random() * 5;

            scoredCars.add(new CarScore(car, score));
        }

        // 3. 排序取 Top 10
        List<Car> result = scoredCars.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score)) // 降序
                .limit(10)
                .map(cs -> cs.car)
                .collect(Collectors.toList());

        // 调试日志
        if (!result.isEmpty()) {
            log.info("用户 {} 推荐列表生成完毕，Top 1: {} (分值: {})", userId, result.get(0).getBrand(), scoredCars.get(0).score);
        }

        return result;
    }

    private Map<String, Object> buildUserProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        
        // 查最近 50 条浏览记录 (样本量大一点)
        List<BrowseLog> logs = browseLogMapper.selectList(new QueryWrapper<BrowseLog>()
                .eq("user_id", userId)
                .orderByDesc("browse_time")
                .last("LIMIT 50"));

        if (logs.isEmpty()) return profile;

        // 统计最喜欢的品牌
        Map<String, Long> brandCount = logs.stream()
                .filter(l -> l.getBrand() != null)
                .collect(Collectors.groupingBy(BrowseLog::getBrand, Collectors.counting()));
        
        if (!brandCount.isEmpty()) {
            String favBrand = brandCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).get().getKey();
            profile.put("favBrand", favBrand);
        }

        // 计算平均浏览价格
        double total = logs.stream().mapToDouble(BrowseLog::getPrice).sum();
        profile.put("avgPrice", total / logs.size());

        return profile;
    }

    @Data
    @AllArgsConstructor
    static class CarScore {
        Car car;
        Double score;
    }
}