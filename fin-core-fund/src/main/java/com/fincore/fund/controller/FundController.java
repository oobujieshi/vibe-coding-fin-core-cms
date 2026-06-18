package com.fincore.fund.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.dto.Result;
import com.fincore.fund.dto.*;
import com.fincore.fund.service.FundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    // ==================== Accounts ====================
    @GetMapping("/accounts")
    public Result<Page<AccountDTO>> listAccounts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String bankName, @RequestParam(required = false) Integer status) {
        return Result.ok(fundService.pageAccounts(page, size, bankName, status));
    }

    @GetMapping("/accounts/{id}")
    public Result<AccountDTO> getAccount(@PathVariable Long id) { return Result.ok(fundService.getAccountById(id)); }

    @PostMapping("/accounts")
    public Result<AccountDTO> createAccount(@RequestBody AccountDTO dto) { return Result.ok(fundService.createAccount(dto)); }

    @PutMapping("/accounts/{id}")
    public Result<AccountDTO> updateAccount(@PathVariable Long id, @RequestBody AccountDTO dto) { return Result.ok(fundService.updateAccount(id, dto)); }

    // ==================== Transactions ====================
    @GetMapping("/transactions")
    public Result<Page<TransactionDTO>> listTransactions(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                                          @RequestParam(required = false) Long accountId, @RequestParam(required = false) Integer transType,
                                                          @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        return Result.ok(fundService.pageTransactions(page, size, accountId, transType, startDate, endDate));
    }

    @GetMapping("/transactions/{id}")
    public Result<TransactionDTO> getTransaction(@PathVariable Long id) { return Result.ok(fundService.getTransactionById(id)); }

    @PostMapping("/transactions")
    public Result<TransactionDTO> createTransaction(@RequestBody TransactionDTO dto) { return Result.ok(fundService.createTransaction(dto)); }

    @PostMapping("/transactions/batch-sync")
    public Result<Map<String, Object>> batchSync(@RequestBody Map<String, Long> body) {
        return Result.ok(fundService.batchSync(body.get("accountId")));
    }

    // ==================== Reconciliations ====================
    @GetMapping("/reconciliations")
    public Result<Page<ReconciliationDTO>> listReconciliations(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) Long accountId) {
        return Result.ok(fundService.pageReconciliations(page, size, accountId));
    }

    @PostMapping("/reconciliations/run")
    public Result<Map<String, Object>> runReconciliation(@RequestBody ReconRunRequest req) { return Result.ok(fundService.runReconciliation(req)); }

    @GetMapping("/reconciliations/diffs")
    public Result<Page<ReconciliationDTO>> listDiffs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.ok(fundService.pageDiffs(page, size));
    }

    @PutMapping("/reconciliations/{id}/handle")
    public Result<Void> handleReconciliation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        fundService.handleReconciliation(id, body.get("action"));
        return Result.ok();
    }
}
