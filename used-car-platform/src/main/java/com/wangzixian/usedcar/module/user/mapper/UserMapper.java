package com.wangzixian.usedcar.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzixian.usedcar.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserMapper 接口
 * 继承 BaseMapper<User> 后，MyBatis-Plus 会自动注入 CRUD 实现
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 暂时无需编写任何方法，BaseMapper 已内置增删改查
}