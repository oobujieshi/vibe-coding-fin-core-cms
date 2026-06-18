package com.fincore.fund.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_fund_transaction")
public class FundTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String transNo;
    private Long accountId;
    private Integer transType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String counterparty;
    private LocalDateTime transTime;
    private Integer source;
    private String summary;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
