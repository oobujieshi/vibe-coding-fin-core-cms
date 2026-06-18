package com.fincore.fund.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_bank_account")
public class BankAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountNo;
    private String bankName;
    private String accountName;
    private BigDecimal balance;
    private String currency;
    private Integer accountStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
