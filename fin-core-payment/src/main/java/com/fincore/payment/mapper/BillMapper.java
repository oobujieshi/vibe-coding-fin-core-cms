package com.fincore.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fincore.payment.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
