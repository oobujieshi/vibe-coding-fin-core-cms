package com.fincore.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReceiptDTO {
    private Long id;
    private String receiptNo;
    private String payerName;
    private BigDecimal amount;
    private Long orderId;
    private Long billId;
    private Integer receiptMethod;
    private String receiptMethodDesc;
    private Integer receiptStatus;
    private String receiptStatusDesc;
    private LocalDateTime confirmedTime;
    private String verifyCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
