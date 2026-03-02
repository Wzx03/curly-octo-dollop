//package com.wangzixian.usedcar.module.order.dto;
//
//public class CreateOrderDto {
//}
package com.wangzixian.usedcar.module.order.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long carId; // 买家只想买这辆车
}