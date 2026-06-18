package com.fincore.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchCalcResult {
    private int totalCount;
    private int successCount;
    private int failCount;
    private List<CalcItem> results;

    @Data
    public static class CalcItem {
        private Long orderId;
        private String orderNo;
        private boolean success;
        private String errorMsg;
        private java.math.BigDecimal originalAmount;
        private java.math.BigDecimal calculatedAmount;
        private Object feeDetail;
    }
}
