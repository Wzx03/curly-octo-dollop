package com.wangzixian.usedcar.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangzixian.usedcar.module.user.entity.TransactionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionLogMapper extends BaseMapper<TransactionLog> {
}