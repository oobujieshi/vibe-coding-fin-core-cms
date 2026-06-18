package com.fincore.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String contractNo;
    private BigDecimal totalAmount;
    private BigDecimal calculatedAmount;
    private BigDecimal feeAmount;
    private String feeDetail;
    private Integer status;
    private String remark;
    private List<OrderItemDTO> items;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
