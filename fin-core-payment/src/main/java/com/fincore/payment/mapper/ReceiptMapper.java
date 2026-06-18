package com.fincore.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fincore.payment.entity.Receipt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReceiptMapper extends BaseMapper<Receipt> {
}
