package com.wangzixian.usedcar.module.car.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarQueryDTO {
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // 筛选参数
    private String energyType;
    private String gearbox;
    private String emissionStandard;
    private String driveMode;
    private String manufacturerType;
    private String displacement; // 排量区间 (例如 "1.1-1.6L")

    // 区间参数 (前端传数字)
    private Integer mileageRange; // 1, 3, 5...
    private Integer carAgeRange;  // 1, 3, 5...
}