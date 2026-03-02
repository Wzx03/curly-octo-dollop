package com.wangzixian.usedcar.module.car.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@TableName("sys_car")
@Table(name="sys_car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;        // 卖家ID
    private String brand;       // 品牌
    private String model;       // 型号
    private BigDecimal price;   // 卖家报价
    private String color;       // 颜色
    private Double mileage;     // 里程 (万公里)
    private String description; // 描述
    private String image;       // 图片
    private Integer status;     // 状态
    private LocalDateTime createTime;

    /** 新车指导价 (万) */
    private Double originalPrice;

    /** 购买年份 */
    private Integer buyYear;

    /** 系统智能估价 (万) */
    private Double estimatedPrice;

    // --- 估价核心因素 ---
    private String conditionGrade;
    private Integer conditionLevel;
    private Integer transferCount;
    private String maintenanceType;
    private String city;

    // --- 热度 ---
    private Integer views;

    // --- 核心参数 ---
    private String displacement; // 排量
    private String gearbox;      // 变速箱

    // 👇👇👇 新增筛选字段 👇👇👇
    private String energyType;       // 能源类型
    private String emissionStandard; // 排放标准
    private String driveMode;        // 驱动方式
    private String manufacturerType; // 厂商属性
}