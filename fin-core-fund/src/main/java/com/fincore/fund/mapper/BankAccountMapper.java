package com.fincore.fund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fincore.fund.entity.BankAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankAccountMapper extends BaseMapper<BankAccount> {
}
