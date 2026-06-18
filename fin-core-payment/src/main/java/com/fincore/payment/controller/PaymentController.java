package com.fincore.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.dto.Result;
import com.fincore.payment.dto.*;
import com.fincore.payment.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final ReceiptService receiptService;
    private final PaymentService paymentService;
    private final BillService billService;
    private final ApprovalService approvalService;

    // ==================== 收款 (Receipts) ====================

    @GetMapping("/receipts")
    public Result<Page<ReceiptDTO>> listReceipts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String payerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(receiptService.page(page, size, payerName, status, orderId, startDate, endDate));
    }

    @GetMapping("/receipts/{id}")
    public Result<ReceiptDTO> getReceipt(@PathVariable Long id) {
        return Result.ok(receiptService.getById(id));
    }

    @PostMapping("/receipts")
    public Result<ReceiptDTO> createReceipt(@RequestBody ReceiptDTO dto) {
        return Result.ok(receiptService.create(dto));
    }

    @PutMapping("/receipts/{id}/confirm")
    public Result<ReceiptDTO> confirmReceipt(@PathVariable Long id) {
        return Result.ok(receiptService.confirm(id));
    }

    @GetMapping("/receipts/{id}/verify-code")
    public Result<Map<String, String>> getVerifyCode(@PathVariable Long id) {
        String code = receiptService.getVerifyCode(id);
        return Result.ok(Map.of("verifyCode", code));
    }

    @PostMapping("/receipts/verify-by-scan")
    public Result<ReceiptDTO> verifyByScan(@RequestBody Map<String, String> body) {
        String verifyCode = body.get("verifyCode");
        return Result.ok(receiptService.verifyByScan(verifyCode));
    }

    // ==================== 付款 (Payments) ====================

    @GetMapping("/payments")
    public Result<Page<PaymentDTO>> listPayments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String payeeName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(paymentService.page(page, size, payeeName, status, startDate, endDate));
    }

    @GetMapping("/payments/{id}")
    public Result<PaymentDTO> getPayment(@PathVariable Long id) {
        return Result.ok(paymentService.getById(id));
    }

    @PostMapping("/payments")
    public Result<PaymentDTO> createPayment(@RequestBody PaymentDTO dto) {
        return Result.ok(paymentService.apply(dto));
    }

    @PutMapping("/payments/{id}/approve")
    public Result<PaymentDTO> approvePayment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String comment = body.getOrDefault("comment", "");
        Long approvedBy = body.containsKey("approvedBy") ? Long.valueOf(body.get("approvedBy")) : 1L;
        return Result.ok(paymentService.approve(id, comment, approvedBy));
    }

    @PutMapping("/payments/{id}/reject")
    public Result<PaymentDTO> rejectPayment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String comment = body.getOrDefault("comment", "");
        Long approvedBy = body.containsKey("approvedBy") ? Long.valueOf(body.get("approvedBy")) : 1L;
        return Result.ok(paymentService.reject(id, comment, approvedBy));
    }

    // ==================== 账单 (Bills) ====================

    @GetMapping("/bills")
    public Result<Page<BillDTO>> listBills(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer sendStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(billService.page(page, size, customerId, sendStatus, startDate, endDate));
    }

    @GetMapping("/bills/{id}")
    public Result<BillDTO> getBill(@PathVariable Long id) {
        return Result.ok(billService.getById(id));
    }

    @PostMapping("/bills")
    public Result<BillDTO> createBill(@RequestBody BillDTO dto) {
        return Result.ok(billService.create(dto));
    }

    @PostMapping("/bills/merge")
    public Result<BillDTO> mergeBills(@RequestBody BillMergeRequest req) {
        return Result.ok(billService.merge(req));
    }

    @PostMapping("/bills/split")
    public Result<List<BillDTO>> splitBill(@RequestBody BillSplitRequest req) {
        return Result.ok(billService.split(req));
    }

    @PostMapping("/bills/{id}/send")
    public Result<Void> sendBill(@PathVariable Long id) {
        billService.send(id);
        return Result.ok();
    }

    // ==================== 审批 (Approvals) ====================

    @GetMapping("/approvals/pending")
    public Result<Page<ApprovalDTO>> pendingApprovals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String bizType) {
        return Result.ok(approvalService.getPendingApprovals(page, size, bizType, null));
    }

    @GetMapping("/approvals/{id}")
    public Result<ApprovalDTO> getApproval(@PathVariable Long id) {
        return Result.ok(approvalService.getApprovalById(id));
    }

    @PutMapping("/approvals/{id}/process")
    public Result<ApprovalDTO> processApproval(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        String comment = body.getOrDefault("comment", "");
        Long approvedBy = body.containsKey("approvedBy") ? Long.valueOf(body.get("approvedBy")) : 1L;
        return Result.ok(approvalService.processApproval(id, action, comment, approvedBy));
    }
}
