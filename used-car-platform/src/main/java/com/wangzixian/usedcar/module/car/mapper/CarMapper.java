//package com.wangzixian.usedcar.module.car.mapper;
//
//public class CarMapper {
//}
package com.wangzixian.usedcar.module.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzixian.usedcar.module.car.entity.Car;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarMapper extends BaseMapper<Car> {
    // 继承 BaseMapper 后，自动拥有了增删改查的能力，一行代码都不用写
}