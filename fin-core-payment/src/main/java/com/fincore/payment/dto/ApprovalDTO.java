package com.fincore.payment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalDTO {
    private Long id;
    private String bizType;
    private Long bizId;
    private Integer currentNode;
    private Integer totalNodes;
    private String nodeConfig;
    private Integer approvalStatus;
    private String approvalStatusDesc;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedTime;
    private String comment;
    private String action;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
