package com.wangzixian.usedcar.module.car.controller;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wangzixian.usedcar.common.RedisUtil;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.common.annotation.Log;
import com.wangzixian.usedcar.module.car.component.RecommendEngine;
import com.wangzixian.usedcar.module.car.component.ValuationEngine;
import com.wangzixian.usedcar.module.car.dto.CarQueryDTO;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.service.CarService;
import com.wangzixian.usedcar.module.order.service.OrderService;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/car")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private ValuationEngine valuationEngine;

    @Autowired
    private RecommendEngine recommendEngine;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String HOME_LIST_CACHE_KEY = "car:list:home";

    private Long getUserIdFromToken(String token) {
        try {
            Object idObj = JWTUtil.parseToken(token).getPayload("id");
            return Long.valueOf(idObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @Log(title = "发布车辆", businessType = 1)
    @PostMapping("/publish")
    public Result publish(@RequestBody Car car, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("无效的Token");

        int currentYear = LocalDateTime.now().getYear();
        if (car.getBuyYear() != null && car.getBuyYear() > currentYear) {
            return Result.error("购买年份不能超过当前年份");
        }

        car.setUserId(userId);
        car.setStatus(0);
        car.setCreateTime(LocalDateTime.now());

        if (car.getConditionGrade() == null) car.setConditionGrade("B");
        if (car.getTransferCount() == null) car.setTransferCount(0);
        if (car.getMaintenanceType() == null) car.setMaintenanceType("SHOP");
        if (car.getCity() == null) car.setCity("其他");
        
        if (car.getDisplacement() == null) car.setDisplacement("2.0T");
        if (car.getGearbox() == null) car.setGearbox("自动");

        Double price = valuationEngine.assess(car);
        car.setEstimatedPrice(price);

        carService.save(car);
        
        // 清除首页缓存
        redisUtil.delete(HOME_LIST_CACHE_KEY);
        
        return Result.success("发布成功，等待审核");
    }

    @GetMapping("/list")
    public Result<IPage<Car>> list(
            @ModelAttribute CarQueryDTO query,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size
    ) {
        // 检查是否为默认首页请求（无筛选条件且第一页）
        boolean isDefaultHome = page == 1 && isQueryEmpty(query);

        if (isDefaultHome) {
            String cache = redisUtil.get(HOME_LIST_CACHE_KEY);
            if (StringUtils.hasText(cache)) {
                try {
                    // 反序列化 Page 对象比较复杂，这里简化处理：缓存 Page 的 records 列表或自定义结构
                    // 为保持一致性，我们这里假设缓存的是 Page<Car> 的 JSON
                    // 注意：Jackson 反序列化 Page 可能需要自定义 Mixin，这里简单起见，如果反序列化失败就查库
                    Page<Car> cachedPage = objectMapper.readValue(cache, new TypeReference<Page<Car>>() {});
                    return Result.success(cachedPage);
                } catch (Exception e) {
                    // 忽略缓存错误，降级查库
                }
            }
        }

        Page<Car> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getStatus, 1);

        if (StringUtils.hasText(query.getBrand())) {
            wrapper.like(Car::getBrand, query.getBrand());
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Car::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Car::getPrice, query.getMaxPrice());
        }

        if (StringUtils.hasText(query.getEnergyType()) && !"不限".equals(query.getEnergyType())) {
            wrapper.eq(Car::getEnergyType, query.getEnergyType());
        }
        if (StringUtils.hasText(query.getGearbox()) && !"不限".equals(query.getGearbox())) {
            wrapper.eq(Car::getGearbox, query.getGearbox());
        }
        if (StringUtils.hasText(query.getEmissionStandard()) && !"不限".equals(query.getEmissionStandard())) {
            wrapper.eq(Car::getEmissionStandard, query.getEmissionStandard());
        }
        if (StringUtils.hasText(query.getDriveMode()) && !"不限".equals(query.getDriveMode())) {
            wrapper.eq(Car::getDriveMode, query.getDriveMode());
        }
        if (StringUtils.hasText(query.getManufacturerType()) && !"不限".equals(query.getManufacturerType())) {
            wrapper.eq(Car::getManufacturerType, query.getManufacturerType());
        }
        if (StringUtils.hasText(query.getDisplacement()) && !"不限".equals(query.getDisplacement())) {
            wrapper.eq(Car::getDisplacement, query.getDisplacement());
        }

        if (query.getMileageRange() != null) {
            wrapper.le(Car::getMileage, query.getMileageRange());
        }
        if (query.getCarAgeRange() != null) {
            int currentYear = LocalDateTime.now().getYear();
            wrapper.ge(Car::getBuyYear, currentYear - query.getCarAgeRange());
        }

        wrapper.orderByDesc(Car::getCreateTime);
        IPage<Car> result = carService.page(pageParam, wrapper);

        // 写入缓存
        if (isDefaultHome && result != null) {
            try {
                String json = objectMapper.writeValueAsString(result);
                redisUtil.set(HOME_LIST_CACHE_KEY, json, 300); // 缓存5分钟
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return Result.success(result);
    }

    private boolean isQueryEmpty(CarQueryDTO query) {
        return !StringUtils.hasText(query.getBrand()) &&
               query.getMinPrice() == null &&
               query.getMaxPrice() == null &&
               (!StringUtils.hasText(query.getEnergyType()) || "不限".equals(query.getEnergyType())) &&
               (!StringUtils.hasText(query.getGearbox()) || "不限".equals(query.getGearbox())) &&
               (!StringUtils.hasText(query.getEmissionStandard()) || "不限".equals(query.getEmissionStandard())) &&
               (!StringUtils.hasText(query.getDriveMode()) || "不限".equals(query.getDriveMode())) &&
               (!StringUtils.hasText(query.getManufacturerType()) || "不限".equals(query.getManufacturerType())) &&
               (!StringUtils.hasText(query.getDisplacement()) || "不限".equals(query.getDisplacement())) &&
               query.getMileageRange() == null &&
               query.getCarAgeRange() == null;
    }

    @GetMapping("/my-cars")
    public Result<IPage<Car>> myCars(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) List<Integer> statusList,
            @RequestHeader("token") String token
    ) {
        Long userId = getUserIdFromToken(token);
        Page<Car> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getUserId, userId);
        
        if (status != null) {
            wrapper.eq(Car::getStatus, status);
        }
        if (statusList != null && !statusList.isEmpty()) {
            wrapper.in(Car::getStatus, statusList);
        }
        
        wrapper.orderByDesc(Car::getCreateTime);

        return Result.success(carService.page(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id, @RequestHeader(value = "token", required = false) String token) {
        Car car = carService.getById(id);
        if (car == null) return Result.error("不存在");

        if (token != null && !token.isEmpty()) {
            Long userId = getUserIdFromToken(token);
            if (userId != null) {
                carService.recordBrowseLog(userId, car);
            }
        }
        return Result.success(car);
    }

    @GetMapping("/recommend")
    public Result getRecommend(@RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");

        List<Car> recommendList = recommendEngine.recommend(userId);
        return Result.success(recommendList);
    }

    @Log(title = "修改车辆", businessType = 2)
    @PostMapping("/update")
    public Result update(@RequestBody Car car, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        
        Car existingCar = carService.getById(car.getId());
        if (existingCar == null) return Result.error("车辆不存在");
        
        boolean isAdmin = user.getRole() == 0;
        boolean isOwner = existingCar.getUserId().equals(userId);
        
        if (!isOwner && !isAdmin) {
            return Result.error("无权操作");
        }

        // 普通用户修改后，强制重置为待审核
        if (!isAdmin) {
            car.setStatus(0); 
        } else {
            // 管理员修改，如果前端没传状态，保持原样
            if (car.getStatus() == null) {
                car.setStatus(existingCar.getStatus());
            }
        }
        
        Double price = valuationEngine.assess(car);
        car.setEstimatedPrice(price);

        boolean success = carService.updateById(car);
        
        // 清除首页缓存
        if (success) redisUtil.delete(HOME_LIST_CACHE_KEY);

        return success ? Result.success("修改成功" + (!isAdmin ? "，请等待审核" : "")) : Result.error("修改失败");
    }

    @Log(title = "下架车辆", businessType = 2)
    @PostMapping("/off-shelf/{id}")
    public Result<String> offShelf(@PathVariable Long id, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        Car car = carService.getById(id);
        
        if (car == null) return Result.error("车辆不存在");
        if (!car.getUserId().equals(userId)) return Result.error("无权操作");
        
        orderService.cancelOrdersByCarId(id);

        car.setStatus(4);
        carService.updateById(car);
        
        // 清除首页缓存
        redisUtil.delete(HOME_LIST_CACHE_KEY);
        
        return Result.success("下架成功");
    }

    @Log(title = "批量下架", businessType = 3)
    @PostMapping("/off-shelf/batch")
    public Result<String> batchOffShelf(@RequestBody List<Long> ids, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (ids == null || ids.isEmpty()) return Result.error("请选择车辆");

        for (Long id : ids) {
            Car car = carService.getById(id);
            if (car != null && car.getUserId().equals(userId)) {
                orderService.cancelOrdersByCarId(id);
            }
        }

        LambdaUpdateWrapper<Car> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Car::getId, ids)
               .eq(Car::getUserId, userId)
               .set(Car::getStatus, 4);
        
        carService.update(null, wrapper);
        
        // 清除首页缓存
        redisUtil.delete(HOME_LIST_CACHE_KEY);
        
        return Result.success("批量下架成功");
    }
    
    @Log(title = "删除车辆", businessType = 3)
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        Car car = carService.getById(id);
        
        if (car == null) return Result.error("车辆不存在");
        if (!car.getUserId().equals(userId)) return Result.error("无权操作");
        
        // 只有下架或未通过审核的可以删除
        if (car.getStatus() == 1 || car.getStatus() == 2) {
            return Result.error("在售或交易中的车辆无法删除");
        }

        carService.removeById(id);
        
        // 清除首页缓存
        redisUtil.delete(HOME_LIST_CACHE_KEY);
        
        return Result.success("删除成功");
    }

    @Data
    @AllArgsConstructor
    class CarScore {
        Car car;
        Double score;
    }
}