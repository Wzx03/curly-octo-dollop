package com.wangzixian.usedcar.module.car.controller;

import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.common.annotation.Log;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.service.AiValuationService;
import com.wangzixian.usedcar.module.car.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/car/ai")
public class AiValuationController {

    @Autowired
    private AiValuationService aiValuationService;

    @Autowired
    private CarService carService;

    @Log(title = "AI生成智能估价报告", businessType = 1)
    @GetMapping("/report/{carId}")
    public Result<String> getAiValuationReport(@PathVariable Long carId) {
        // 先从数据库把车查出来
        Car car = carService.getById(carId);
        if (car == null) {
            return Result.error("车辆不存在");
        }

        // 调用 AI 服务生成长文本报告
        String aiReport = aiValuationService.generateValuationReport(car);
        
        return Result.success(aiReport);
    }
}