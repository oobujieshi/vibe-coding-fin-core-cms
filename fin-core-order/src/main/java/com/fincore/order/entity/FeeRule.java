package com.fincore.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_fee_rule")
public class FeeRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private Integer ruleType;
    private String ruleConfig;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private Integer isActive;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
