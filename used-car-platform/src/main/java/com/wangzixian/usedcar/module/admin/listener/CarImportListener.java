package com.wangzixian.usedcar.module.admin.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.wangzixian.usedcar.module.admin.dto.CarImportDTO;
import com.wangzixian.usedcar.module.car.component.ValuationEngine;
import com.wangzixian.usedcar.module.car.entity.Car;
import com.wangzixian.usedcar.module.car.service.CarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CarImportListener implements ReadListener<CarImportDTO> {

    private final CarService carService;
    private final ValuationEngine valuationEngine;
    private final Long operatorId;
    
    private static final int BATCH_COUNT = 100;
    private List<Car> cachedDataList = new ArrayList<>(BATCH_COUNT);

    public CarImportListener(CarService carService, ValuationEngine valuationEngine, Long operatorId) {
        this.carService = carService;
        this.valuationEngine = valuationEngine;
        this.operatorId = operatorId;
    }

    @Override
    public void invoke(CarImportDTO data, AnalysisContext context) {
        Car car = new Car();
        BeanUtils.copyProperties(data, car);
        
        // 导入的车辆默认进入审核队列 (status = 0)
        car.setStatus(0);
        car.setCreateTime(LocalDateTime.now());
        // 设置发布人为当前管理员
        car.setUserId(operatorId);
        
        if (car.getConditionGrade() == null) car.setConditionGrade("B");
        if (car.getConditionLevel() == null) car.setConditionLevel(2);
        if (car.getTransferCount() == null) car.setTransferCount(0);
        
        if (!StringUtils.hasText(car.getDisplacement())) car.setDisplacement("2.0T");
        if (!StringUtils.hasText(car.getGearbox())) car.setGearbox("自动");
        if (!StringUtils.hasText(car.getEnergyType())) car.setEnergyType("汽油");
        if (!StringUtils.hasText(car.getEmissionStandard())) car.setEmissionStandard("国VI");
        if (!StringUtils.hasText(car.getDriveMode())) car.setDriveMode("前驱");
        if (!StringUtils.hasText(car.getManufacturerType())) car.setManufacturerType("合资");
        
        Double price = valuationEngine.assess(car);
        car.setEstimatedPrice(price);
        
        cachedDataList.add(car);

        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        if (!cachedDataList.isEmpty()) {
            log.info("正在插入 {} 条数据...", cachedDataList.size());
            carService.saveBatch(cachedDataList);
            cachedDataList.clear();
        }
    }
}