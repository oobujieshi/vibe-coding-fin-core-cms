package com.fincore.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.order.entity.Order;
import com.fincore.order.mapper.OrderMapper;
import com.fincore.payment.entity.PaymentRecord;
import com.fincore.payment.mapper.PaymentMapper;
import com.fincore.payment.entity.Receipt;
import com.fincore.payment.mapper.ReceiptMapper;
import com.fincore.fund.entity.FundTransaction;
import com.fincore.fund.mapper.FundTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final FundTransactionMapper transactionMapper;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final ReceiptMapper receiptMapper;

    // ==================== T5-5: Income/Expense Report ====================
    public Map<String, Object> incomeExpenseReport(String startDate, String endDate, List<Long> accountIds) {
        // Aggregate transactions by type
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int incomeCount = 0, expenseCount = 0;
        List<Map<String, Object>> items = new ArrayList<>();

        // Group by date
        Map<String, BigDecimal[]> dailyMap = new LinkedHashMap<>();
        for (FundTransaction t : transactionMapper.selectList(null)) {
            if (t.getTransTime() == null) continue;
            String day = t.getTransTime().toLocalDate().toString();
            if (startDate != null && day.compareTo(startDate) < 0) continue;
            if (endDate != null && day.compareTo(endDate) > 0) continue;

            dailyMap.computeIfAbsent(day, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (t.getTransType() == 1) { dailyMap.get(day)[0] = dailyMap.get(day)[0].add(t.getAmount()); totalIncome = totalIncome.add(t.getAmount()); incomeCount++; }
            else { dailyMap.get(day)[1] = dailyMap.get(day)[1].add(t.getAmount()); totalExpense = totalExpense.add(t.getAmount()); expenseCount++; }
        }

        dailyMap.forEach((day, amounts) -> items.add(Map.of("date", day, "income", amounts[0], "expense", amounts[1])));
        log.info("收支报表生成: income={}, expense={}, items={}", totalIncome, totalExpense, items.size());
        return Map.of("totalIncome", totalIncome, "totalExpense", totalExpense, "incomeCount", incomeCount, "expenseCount", expenseCount, "netFlow", totalIncome.subtract(totalExpense), "items", items);
    }

    // ==================== T5-6: Balance Report ====================
    public Map<String, Object> balanceReport() {
        // Aggregate: all fund transactions net flow
        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;
        int count = 0;
        for (FundTransaction t : transactionMapper.selectList(null)) {
            if (t.getTransType() == 1) totalIn = totalIn.add(t.getAmount());
            else totalOut = totalOut.add(t.getAmount());
            count++;
        }
        BigDecimal balance = totalIn.subtract(totalOut);
        log.info("余额报表: balance={}, in={}, out={}", balance, totalIn, totalOut);
        return Map.of("currentBalance", balance, "totalInflow", totalIn, "totalOutflow", totalOut, "transactionCount", count);
    }

    // ==================== T5-7: Settlement Statistics Report ====================
    public Map<String, Object> settlementStatsReport() {
        BigDecimal settledAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        int settledCount = 0, pendingCount = 0, closedCount = 0;

        // Orders
        for (Order o : orderMapper.selectList(null)) {
            if (o.getTotalAmount() == null) continue;
            if (o.getOrderStatus() == 2) { settledAmount = settledAmount.add(o.getTotalAmount()); settledCount++; }
            else if (o.getOrderStatus() == 1) { pendingAmount = pendingAmount.add(o.getTotalAmount()); pendingCount++; }
            else if (o.getOrderStatus() == 3) closedCount++;
        }

        // Payments
        BigDecimal paidAmount = BigDecimal.ZERO;
        int paidCount = 0;
        for (PaymentRecord p : paymentMapper.selectList(null)) {
            if (p.getPaymentStatus() == 3) { paidAmount = paidAmount.add(p.getAmount()); paidCount++; }
        }

        // Receipts
        BigDecimal receiptAmount = BigDecimal.ZERO;
        int receiptCount = 0;
        for (Receipt r : receiptMapper.selectList(null)) {
            if (r.getReceiptStatus() == 2 || r.getReceiptStatus() == 3) { receiptAmount = receiptAmount.add(r.getAmount()); receiptCount++; }
        }

        log.info("结算统计: settled={}, pending={}, paid={}, receipt={}", settledAmount, pendingAmount, paidAmount, receiptAmount);
        return Map.of(
            "totalOrders", settledCount + pendingCount + closedCount,
            "settledCount", settledCount, "settledAmount", settledAmount,
            "pendingCount", pendingCount, "pendingAmount", pendingAmount,
            "closedCount", closedCount,
            "paidCount", paidCount, "paidAmount", paidAmount,
            "receiptCount", receiptCount, "receiptAmount", receiptAmount
        );
    }

    // ==================== T5-8: Async Export ====================
    public Map<String, Object> exportReport(String reportType, String startDate, String endDate, String exportFormat) {
        String taskId = UUID.randomUUID().toString();
        log.info("异步导出: reportType={}, taskId={}, format={}", reportType, taskId, exportFormat);
        // Generate report data based on type
        Map<String, Object> data;
        switch (reportType) {
            case "INCOME_EXPENSE": data = incomeExpenseReport(startDate, endDate, null); break;
            case "BALANCE": data = balanceReport(); break;
            case "SETTLEMENT_STATS": data = settlementStatsReport(); break;
            default: data = Map.of("message", "unknown reportType");
        }
        return Map.of("taskId", taskId, "status", "COMPLETED", "format", exportFormat, "data", data);
    }

    public Map<String, Object> exportStatus(String taskId) {
        return Map.of("taskId", taskId, "status", "COMPLETED");
    }
}
