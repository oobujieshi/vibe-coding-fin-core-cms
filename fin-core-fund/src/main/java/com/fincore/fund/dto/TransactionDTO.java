package com.fincore.fund.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private String transNo;
    private Long accountId;
    private Integer transType;
    private String transTypeDesc;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String counterparty;
    private LocalDateTime transTime;
    private Integer source;
    private String sourceDesc;
    private String summary;
    private LocalDateTime createTime;
}
