package com.fincore.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchCalcRequest {
    private List<Long> orderIds;
}
