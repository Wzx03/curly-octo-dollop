package com.wangzixian.usedcar.module.admin.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarImportDTO {
    @ExcelProperty("品牌")
    private String brand;

    @ExcelProperty("型号")
    private String model;

    @ExcelProperty("价格(万)")
    private BigDecimal price;

    @ExcelProperty("新车价(万)")
    private Double originalPrice;

    @ExcelProperty("年份")
    private Integer buyYear;

    @ExcelProperty("里程(万公里)")
    private Double mileage;
    
    @ExcelProperty("颜色")
    private String color;
    
    @ExcelProperty("描述")
    private String description;

    // 👇 新增核心字段
    @ExcelProperty("排量")
    private String displacement;

    @ExcelProperty("变速箱")
    private String gearbox;

    @ExcelProperty("能源类型")
    private String energyType;

    @ExcelProperty("排放标准")
    private String emissionStandard;

    @ExcelProperty("驱动方式")
    private String driveMode;

    @ExcelProperty("厂商属性")
    private String manufacturerType;
}