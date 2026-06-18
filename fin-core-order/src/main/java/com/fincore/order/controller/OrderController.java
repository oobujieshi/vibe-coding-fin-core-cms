package com.fincore.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.dto.Result;
import com.fincore.order.dto.*;
import com.fincore.order.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ========== 订单 CRUD ==========

    @GetMapping("/orders")
    public Result<Page<OrderDTO>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(orderService.page(page, size, customerName, status, startDate, endDate));
    }

    @GetMapping("/orders/{id}")
    public Result<OrderDTO> getOrder(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @PostMapping("/orders")
    public Result<OrderDTO> createOrder(@RequestBody OrderDTO dto) {
        return Result.ok(orderService.create(dto));
    }

    @PutMapping("/orders/{id}")
    public Result<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO dto) {
        return Result.ok(orderService.update(id, dto));
    }

    @DeleteMapping("/orders/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return Result.ok();
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        orderService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    // ========== 批量费用计算 ==========

    @PostMapping("/orders/batch-calc")
    public Result<BatchCalcResult> batchCalc(@RequestBody BatchCalcRequest req) {
        return Result.ok(orderService.batchCalc(req));
    }

    // ========== 结算单 ==========

    @GetMapping("/settlements")
    public Result<Page<SettlementDTO>> listSettlements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(orderService.pageSettlements(page, size, orderId, status, startDate, endDate));
    }

    @GetMapping("/settlements/{id}")
    public Result<SettlementDTO> getSettlement(@PathVariable Long id) {
        return Result.ok(orderService.getSettlementById(id));
    }

    @PostMapping("/settlements")
    public Result<List<SettlementDTO>> createSettlements(@RequestBody java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("orderIds");
        List<Long> orderIds = ids.stream().map(Long::valueOf).toList();
        String remark = (String) body.get("remark");
        return Result.ok(orderService.createSettlements(orderIds, remark));
    }

    @PutMapping("/settlements/{id}/confirm")
    public Result<Void> confirmSettlement(@PathVariable Long id) {
        orderService.confirmSettlement(id);
        return Result.ok();
    }

    @PutMapping("/settlements/{id}/cancel")
    public Result<Void> cancelSettlement(@PathVariable Long id) {
        orderService.cancelSettlement(id);
        return Result.ok();
    }

    // ========== 费率规则 ==========

    @GetMapping("/fee-rules")
    public Result<Page<FeeRuleDTO>> listFeeRules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) Integer status) {
        return Result.ok(orderService.pageFeeRules(page, size, ruleName, status));
    }

    @PostMapping("/fee-rules")
    public Result<FeeRuleDTO> createFeeRule(@RequestBody FeeRuleDTO dto) {
        return Result.ok(orderService.createFeeRule(dto));
    }

    @PutMapping("/fee-rules/{id}")
    public Result<FeeRuleDTO> updateFeeRule(@PathVariable Long id, @RequestBody FeeRuleDTO dto) {
        return Result.ok(orderService.updateFeeRule(id, dto));
    }

    @PutMapping("/fee-rules/{id}/toggle")
    public Result<Void> toggleFeeRule(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        orderService.toggleFeeRule(id, body.get("status"));
        return Result.ok();
    }

    // ========== Excel 导入导出 ==========

    @PostMapping("/orders/import")
    public Result<Map<String, Object>> importOrders(@RequestParam("file") MultipartFile file) {
        try {
            List<OrderDTO> orders = ExcelImportUtil.parseOrders(file);
            int success = 0, fail = 0;
            List<Map<String, Object>> errors = new ArrayList<>();
            for (OrderDTO dto : orders) {
                try { orderService.create(dto); success++; }
                catch (Exception e) { fail++; errors.add(Map.of("row", success + fail, "message", e.getMessage())); }
            }
            return Result.ok(Map.of("totalRows", orders.size(), "successRows", success, "failRows", fail, "errors", errors));
        } catch (Exception e) {
            log.error("Import failed", e);
            return Result.fail(400, "导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/orders/export")
    public void exportOrders(@RequestParam(required = false) Integer status,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              @RequestParam(required = false) String ids,
                              HttpServletResponse response) throws Exception {
        List<Long> idList = ids != null ? Arrays.stream(ids.split(",")).map(Long::valueOf).toList() : List.of();
        List<OrderDTO> data = orderService.page(1, 9999, null, status, startDate, endDate).getRecords();
        if (!idList.isEmpty()) data = data.stream().filter(o -> idList.contains(o.getId())).toList();

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("订单导出.csv", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        ExcelExportUtil.write(response.getOutputStream(), data);
    }
}

class ExcelImportUtil {
    static List<OrderDTO> parseOrders(MultipartFile file) throws Exception {
        String raw = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        // strip BOM if present
        if (!raw.isEmpty() && raw.charAt(0) == '\uFEFF') raw = raw.substring(1);
        String[] lines = raw.split("\\r?\\n");
        List<OrderDTO> list = new java.util.ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            // skip header
            if (i == 0 && (line.startsWith("订单号") || line.startsWith("orderNo"))) continue;
            String[] cols = line.split(",");
            if (cols.length < 4) continue;
            try {
                OrderDTO o = new OrderDTO();
                o.setCustomerName(cols.length > 1 ? cols[1].trim() : "");
                o.setContractNo(cols.length > 2 ? cols[2].trim() : "");
                o.setTotalAmount(new java.math.BigDecimal(cols.length > 3 ? cols[3].trim() : "0"));
                list.add(o);
            } catch (Exception e) {
                throw new RuntimeException("第" + (i + 1) + "行解析失败: " + e.getMessage());
            }
        }
        if (list.isEmpty()) throw new RuntimeException("CSV 为空或无有效数据");
        return list;
    }
}

class ExcelExportUtil {
    static void write(java.io.OutputStream os, List<OrderDTO> data) throws Exception {
        os.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}); // UTF-8 BOM
        StringBuilder sb = new StringBuilder("订单号,客户,合同号,金额,状态,创建时间\n");
        for (OrderDTO o : data) {
            sb.append(o.getOrderNo()).append(",").append(o.getCustomerName()).append(",")
              .append(o.getContractNo()).append(",").append(o.getTotalAmount()).append(",")
              .append(o.getStatus()).append(",").append(o.getCreateTime()).append("\n");
        }
        os.write(sb.toString().getBytes("UTF-8"));
    }
}
