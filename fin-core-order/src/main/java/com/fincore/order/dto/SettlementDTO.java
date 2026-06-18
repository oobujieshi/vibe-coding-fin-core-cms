package com.fincore.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SettlementDTO {
    private Long id;
    private String settlementNo;
    private Long orderId;
    private String orderNo;
    private String customerName;
    private BigDecimal originalAmount;
    private Map<String, Object> feeDetail;
    private BigDecimal realAmount;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
