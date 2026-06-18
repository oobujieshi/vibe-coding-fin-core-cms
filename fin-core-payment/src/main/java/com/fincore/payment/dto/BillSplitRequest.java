package com.fincore.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BillSplitRequest {
    private Long billId;
    private List<BigDecimal> splitAmounts;
}
