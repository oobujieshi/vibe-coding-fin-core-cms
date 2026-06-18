package com.fincore.fund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fincore.fund.entity.FundTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FundTransactionMapper extends BaseMapper<FundTransaction> {
}
