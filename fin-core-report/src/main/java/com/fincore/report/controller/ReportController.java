package com.fincore.report.controller;

import com.fincore.common.base.dto.Result;
import com.fincore.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/reports/income-expense")
    public Result<Map<String, Object>> incomeExpense(@RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate) {
        return Result.ok(reportService.incomeExpenseReport(startDate, endDate, null));
    }

    @GetMapping("/reports/balance")
    public Result<Map<String, Object>> balance() {
        return Result.ok(reportService.balanceReport());
    }

    @GetMapping("/reports/settlement-stats")
    public Result<Map<String, Object>> settlementStats() {
        return Result.ok(reportService.settlementStatsReport());
    }

    @PostMapping("/reports/export")
    public Result<Map<String, Object>> export(@RequestBody Map<String, Object> body) {
        return Result.ok(reportService.exportReport(
            (String) body.get("reportType"),
            (String) body.get("startDate"),
            (String) body.get("endDate"),
            (String) body.getOrDefault("exportFormat", "EXCEL").toString()
        ));
    }

    @GetMapping("/reports/export/status")
    public Result<Map<String, Object>> exportStatus(@RequestParam String taskId) {
        return Result.ok(reportService.exportStatus(taskId));
    }
}
