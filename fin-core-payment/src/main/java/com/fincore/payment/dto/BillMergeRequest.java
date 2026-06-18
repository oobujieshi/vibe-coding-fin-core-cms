package com.fincore.payment.dto;

import lombok.Data;
import java.util.List;

@Data
public class BillMergeRequest {
    private List<Long> billIds;
    private String customerName;
}
