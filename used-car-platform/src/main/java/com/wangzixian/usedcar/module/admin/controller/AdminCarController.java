package com.wangzixian.usedcar.module.admin.controller;

import cn.hutool.jwt.JWTUtil; // 👈 引入
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.common.annotation.Log;
import com.wangzixian.usedcar.module.admin.dto.CarImportDTO;
import com.wangzixian.usedcar.module.admin.listener.CarImportListener;
import com.wangzixian.usedcar.module.car.component.ValuationEngine;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.service.CarService;
import com.wangzixian.usedcar.module.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/car")
public class AdminCarController {

    @Autowired
    private CarService carService;

    @Autowired
    private ValuationEngine valuationEngine;

    @Autowired
    private OrderService orderService;

    @GetMapping("/audit/list")
    public Result<Page<Car>> auditList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<Car> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getStatus, 0);
        wrapper.orderByDesc(Car::getCreateTime);
        
        return Result.success(carService.page(pageParam, wrapper));
    }
    
    @GetMapping("/list")
    public Result<Page<Car>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        Page<Car> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Car::getBrand, keyword)
                            .or().like(Car::getModel, keyword));
        }

        if (status != null) {
            wrapper.eq(Car::getStatus, status);
        }
        
        wrapper.orderByDesc(Car::getCreateTime);
        return Result.success(carService.page(pageParam, wrapper));
    }

    @Log(title = "审核通过", businessType = 2)
    @PostMapping("/approve/{id}")
    public Result<String> approve(@PathVariable Long id) {
        Car car = carService.getById(id);
        if (car == null) return Result.error("车辆不存在");
        
        car.setStatus(1);
        carService.updateById(car);
        return Result.success("审核通过，已上架");
    }
    
    @Log(title = "批量审核通过", businessType = 2)
    @PostMapping("/batch-approve")
    public Result<String> batchApprove(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("请选择车辆");
        
        LambdaUpdateWrapper<Car> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Car::getId, ids).set(Car::getStatus, 1);
        carService.update(null, wrapper);
        
        return Result.success("批量通过成功");
    }

    @Log(title = "审核驳回", businessType = 2)
    @PostMapping("/reject/{id}")
    public Result<String> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Car car = carService.getById(id);
        if (car == null) return Result.error("车辆不存在");
        
        String reason = body != null ? body.get("reason") : "无";
        
        car.setStatus(-1); // -1 代表驳回
        // 将驳回原因追加到描述中
        String newDesc = "【驳回原因】: " + reason + "\n" + (car.getDescription() != null ? car.getDescription() : "");
        car.setDescription(newDesc);
        
        carService.updateById(car);
        return Result.success("已驳回");
    }
    
    @Log(title = "批量驳回", businessType = 2)
    @PostMapping("/batch-reject")
    public Result<String> batchReject(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("请选择车辆");
        
        LambdaUpdateWrapper<Car> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Car::getId, ids).set(Car::getStatus, -1);
        carService.update(null, wrapper);
        
        return Result.success("批量驳回成功");
    }

    @Log(title = "批量导入", businessType = 1)
    @PostMapping("/import")
    public Result<String> importCars(MultipartFile file, @RequestHeader("token") String token) { // 👈 增加 token 参数
        try {
            // 解析 Token 获取当前管理员 ID
            Long operatorId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());

            // 传入 operatorId
            EasyExcel.read(file.getInputStream(), CarImportDTO.class, new CarImportListener(carService, valuationEngine, operatorId))
                    .sheet()
                    .doRead();
            
            return Result.success("导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @Log(title = "修改车辆", businessType = 2)
    @RequestMapping(value = "/update", method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<String> update(@RequestBody Car car) {
        if (car.getId() == null) return Result.error("ID不能为空");
        carService.updateById(car);
        return Result.success("修改成功");
    }

    @Log(title = "修改车辆", businessType = 2)
    @RequestMapping(value = "/update/{id}", method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<String> updateByIdPath(@PathVariable Long id, @RequestBody Car car) {
        car.setId(id);
        return update(car);
    }

    @Log(title = "批量下架", businessType = 3)
    @PostMapping("/batch-off-shelf")
    public Result<String> batchOffShelf(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("请选择车辆");
        
        // 1. 取消关联订单
        for (Long id : ids) {
            orderService.cancelOrdersByCarId(id);
        }

        // 2. 批量更新状态
        LambdaUpdateWrapper<Car> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Car::getId, ids).set(Car::getStatus, 4); // 4-已下架
        
        carService.update(null, wrapper);
        return Result.success("批量下架成功");
    }
    
    @Log(title = "删除车辆", businessType = 3)
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        carService.removeById(id);
        return Result.success("删除成功");
    }
}