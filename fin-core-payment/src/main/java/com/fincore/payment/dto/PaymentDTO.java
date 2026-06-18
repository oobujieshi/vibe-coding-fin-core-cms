package com.fincore.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private String paymentNo;
    private String payeeName;
    private BigDecimal amount;
    private Long supplierId;
    private Long orderId;
    private Integer paymentStatus;
    private String paymentStatusDesc;
    private Long appliedBy;
    private LocalDateTime appliedTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
