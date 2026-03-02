package com.wangzixian.usedcar.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity // 👈 补上这个
@TableName("sys_order")
@Table(name="sys_order") // 👈 补上这个
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;     // 订单号
    private Long carId;         // 车ID
    private Long buyerId;       // 买家ID
    private Long sellerId;      // 卖家ID
    private BigDecimal totalPrice; // 成交价
    private Integer status;     // 0-未付 1-已付 2-取消
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}