package com.fincore.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillDTO {
    private Long id;
    private String billNo;
    private Long customerId;
    private String customerName;
    private LocalDate billPeriodStart;
    private LocalDate billPeriodEnd;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
    private Integer sendStatus;
    private String sendStatusDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
