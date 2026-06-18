package com.fincore.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_settlement")
public class Settlement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String settlementNo;
    private Long orderId;
    private BigDecimal calculatedAmount;
    private String feeDetail;
    private Integer settlementStatus;
    private Long settledBy;
    private LocalDateTime settledTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
