package com.fincore.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fincore.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
