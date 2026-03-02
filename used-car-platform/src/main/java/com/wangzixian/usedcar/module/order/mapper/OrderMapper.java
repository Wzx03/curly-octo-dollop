//package com.wangzixian.usedcar.module.order.mapper;
//
//public class OrderMapper {
//}
package com.wangzixian.usedcar.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzixian.usedcar.module.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}