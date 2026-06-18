package com.fincore.fund.dto;

import lombok.Data;

@Data
public class ReconRunRequest {
    private Long accountId;
    private String startDate;
    private String endDate;
}
