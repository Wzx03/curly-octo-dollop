//package com.wangzixian.usedcar.module.admin.controller;
//
//public class AdminController {
//}
package com.wangzixian.usedcar.module.admin.controller;

import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CarService carService;

    // 1. 获取所有"待审核"的车辆
    @GetMapping("/pending-cars")
    public Result<List<Car>> getPendingCars() {
        // 利用 QueryWrapper 查 status = 0 的车
        return Result.success(carService.lambdaQuery().eq(Car::getStatus, 0).list());
    }

    // 2. 审核通过接口
    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long carId) {
        Car car = carService.getById(carId);
        if (car == null) {
            return Result.error("车辆不存在");
        }

        // 把状态从 0 改成 1 (上架)
        car.setStatus(1);
        carService.updateById(car);

        return Result.success("审核通过，已上架！");
    }

    // 3. 审核拒绝/下架接口
    @PostMapping("/reject")
    public Result<String> reject(@RequestParam Long carId) {
        Car car = carService.getById(carId);
        if (car == null) {
            return Result.error("车辆不存在");
        }

        // 设为 -1 代表被驳回/下架 (需要在实体类注释里补充这个状态，或者直接物理删除)
        // 这里演示简单修改状态
        car.setStatus(-1);
        carService.updateById(car);

        return Result.success("已驳回");
    }
}