package com.wangzixian.usedcar.module.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wangzixian.usedcar.common.RedisUtil;
import com.wangzixian.usedcar.module.car.entity.BrowseLog;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.mapper.BrowseLogMapper;
import com.wangzixian.usedcar.module.car.mapper.CarMapper;
import com.wangzixian.usedcar.module.car.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service
public class CarServiceImpl extends ServiceImpl<CarMapper, Car> implements CarService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BrowseLogMapper browseLogMapper;

    private static final String CACHE_KEY_PREFIX = "car:detail:";

    @Override
    public void publishCar(Car car) {
        car.setCreateTime(LocalDateTime.now());
        car.setStatus(0);
        this.save(car);
    }

    @Override
    public Car getById(Serializable id) {
        String key = CACHE_KEY_PREFIX + id;
        String json = redisUtil.get(key);
        if (StringUtils.hasText(json)) {
            try {
                return objectMapper.readValue(json, Car.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Car car = super.getById(id);
        if (car != null) {
            try {
                String newJson = objectMapper.writeValueAsString(car);
                redisUtil.set(key, newJson, 3600);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return car;
    }

    @Override
    public boolean updateById(Car entity) {
        boolean success = super.updateById(entity);
        if (success) {
            redisUtil.delete(CACHE_KEY_PREFIX + entity.getId());
        }
        return success;
    }

    @Async
    @Override
    public void recordBrowseLog(Long userId, Car car) {
        try {
            BrowseLog log = new BrowseLog();
            log.setUserId(userId.intValue());
            log.setCarId(Long.valueOf(car.getId()).intValue());
            log.setBrand(car.getBrand());
            log.setPrice(car.getPrice() != null ? car.getPrice().doubleValue() : 0.0);
            log.setBrowseTime(LocalDateTime.now());
            
            browseLogMapper.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}