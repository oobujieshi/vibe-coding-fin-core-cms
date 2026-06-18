package com.fincore.fund.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_reconciliation")
public class Reconciliation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long systemTransId;
    private String actualTransId;
    private Integer diffType;
    private BigDecimal diffAmount;
    private Integer handleStatus;
    private Long handleBy;
    private LocalDateTime handleTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
