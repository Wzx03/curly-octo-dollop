package com.wangzixian.usedcar.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.mapper.CarMapper;
import com.wangzixian.usedcar.module.order.entity.Order;
import com.wangzixian.usedcar.module.order.mapper.OrderMapper;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import com.wangzixian.usedcar.module.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CarMapper carMapper;
    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        try {
            Map<String, Object> data = new HashMap<>();

            // 1. 核心指标 (真实)
            data.put("totalUsers", userMapper.selectCount(null));
            data.put("totalCars", carMapper.selectCount(null));
            
            QueryWrapper<Order> orderQuery = new QueryWrapper<>();
            orderQuery.eq("status", 1);
            List<Order> paidOrders = orderMapper.selectList(orderQuery);
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (paidOrders != null) {
                totalAmount = paidOrders.stream()
                        .map(Order::getTotalPrice)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            data.put("totalAmount", totalAmount);
            
            QueryWrapper<Car> todayCarQuery = new QueryWrapper<>();
            todayCarQuery.ge("create_time", LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
            data.put("todayNewCars", carMapper.selectCount(todayCarQuery));

            // 2. 品牌分布 (真实)
            List<Car> allCars = carMapper.selectList(null);
            List<Map<String, Object>> brandChart = new ArrayList<>();
            if (allCars != null) {
                Map<String, Long> brandMap = allCars.stream()
                        .filter(c -> c.getBrand() != null)
                        .collect(Collectors.groupingBy(Car::getBrand, Collectors.counting()));
                
                brandMap.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .limit(5)
                        .forEach(e -> {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", e.getKey());
                            item.put("value", e.getValue());
                            brandChart.add(item);
                        });
            }
            data.put("brandChart", brandChart);

            // 3. 近7天交易走势 (真实)
            List<String> dates = new ArrayList<>();
            List<Integer> volumes = new ArrayList<>();
            
            // 查最近7天的所有订单
            QueryWrapper<Order> weekOrderQuery = new QueryWrapper<>();
            weekOrderQuery.ge("create_time", LocalDateTime.now().minusDays(6).with(LocalTime.MIN));
            List<Order> weekOrders = orderMapper.selectList(weekOrderQuery);

            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.toString().substring(5); // MM-dd
                dates.add(dateStr);
                
                // 统计当天的订单数
                long count = weekOrders.stream()
                        .filter(o -> o.getCreateTime().toLocalDate().equals(date))
                        .count();
                volumes.add((int) count);
            }
            data.put("trendDates", dates);
            data.put("trendVolumes", volumes);

            // 4. 价格区间分布 (真实)
            // 区间: 0-5, 5-10, 10-20, 20-50, 50+
            int[] priceRanges = new int[5]; // [0, 0, 0, 0, 0]
            if (allCars != null) {
                for (Car c : allCars) {
                    if (c.getPrice() == null) continue;
                    double p = c.getPrice().doubleValue();
                    if (p < 5) priceRanges[0]++;
                    else if (p < 10) priceRanges[1]++;
                    else if (p < 20) priceRanges[2]++;
                    else if (p < 50) priceRanges[3]++;
                    else priceRanges[4]++;
                }
            }
            // 构造雷达图数据结构
            List<Map<String, Object>> radarData = new ArrayList<>();
            Map<String, Object> radarItem = new HashMap<>();
            radarItem.put("value", priceRanges);
            radarItem.put("name", "车源数量");
            radarData.add(radarItem);
            data.put("priceChart", radarData); // 注意这里 key 叫 priceChart

            // 5. 最新成交订单 (真实)
            QueryWrapper<Order> latestOrderQuery = new QueryWrapper<>();
            latestOrderQuery.eq("status", 1);
            latestOrderQuery.orderByDesc("pay_time");
            latestOrderQuery.last("LIMIT 5");
            List<Order> latestOrders = orderMapper.selectList(latestOrderQuery);

            List<Map<String, Object>> orderList = new ArrayList<>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd HH:mm");

            for (Order order : latestOrders) {
                Map<String, Object> item = new HashMap<>();
                item.put("price", order.getTotalPrice());
                item.put("time", order.getPayTime() != null ? order.getPayTime().format(dtf) : "刚刚");

                User buyer = userMapper.selectById(order.getBuyerId());
                item.put("nickname", buyer != null ? buyer.getNickname() : "未知用户");
                item.put("avatar", buyer != null ? buyer.getAvatar() : "");

                Car car = carMapper.selectById(order.getCarId());
                item.put("carModel", car != null ? car.getBrand() + " " + car.getModel() : "未知车型");
                
                orderList.add(item);
            }
            data.put("latestOrders", orderList);

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取数据失败");
        }
    }
}