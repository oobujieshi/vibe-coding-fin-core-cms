package com.fincore.fund.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private Long id;
    private String accountNo;
    private String bankName;
    private String accountName;
    private BigDecimal balance;
    private String currency;
    private Integer accountStatus;
    private String accountStatusDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
