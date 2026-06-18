package com.fincore.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_receipt")
public class Receipt {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String receiptNo;
    private String payerName;
    private BigDecimal amount;
    private Long orderId;
    private Long billId;
    private Integer receiptMethod;
    private Integer receiptStatus;
    private LocalDateTime confirmedTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
