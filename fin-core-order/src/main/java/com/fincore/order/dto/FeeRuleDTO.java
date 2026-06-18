package com.fincore.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FeeRuleDTO {
    private Long id;
    private String ruleName;
    private String ruleType;
    private BigDecimal rate;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private Map<String, Object> conditions;
    private Integer priority;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
