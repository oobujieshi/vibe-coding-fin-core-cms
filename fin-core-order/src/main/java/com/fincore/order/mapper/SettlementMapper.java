package com.fincore.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fincore.order.entity.Settlement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettlementMapper extends BaseMapper<Settlement> {
}
