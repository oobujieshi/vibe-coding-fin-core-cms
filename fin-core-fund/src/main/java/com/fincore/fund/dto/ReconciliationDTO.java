package com.fincore.fund.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReconciliationDTO {
    private Long id;
    private String transNo;
    private String bankTransNo;
    private BigDecimal sysAmount;
    private BigDecimal bankAmount;
    private BigDecimal diffAmount;
    private Integer matchStatus;
    private String matchStatusDesc;
    private String detail;
    private LocalDateTime createTime;
}
