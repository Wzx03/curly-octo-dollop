package com.wangzixian.usedcar.module.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzixian.usedcar.module.car.entity.Car;

public interface CarService extends IService<Car> {

    void publishCar(Car car);

    /**
     * 异步记录浏览日志
     */
    void recordBrowseLog(Long userId, Car car);
}