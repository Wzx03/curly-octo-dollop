package com.wangzixian.usedcar.module.order.vo;

import com.wangzixian.usedcar.module.order.entity.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {
    private String carBrand;
    private String carModel;
    private String carImage;
    private Integer carYear;
}