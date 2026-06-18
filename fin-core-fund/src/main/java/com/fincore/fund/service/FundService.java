package com.fincore.fund.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.exception.BusinessException;
import com.fincore.fund.dto.*;
import com.fincore.fund.entity.*;
import com.fincore.fund.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundService {

    private final BankAccountMapper accountMapper;
    private final FundTransactionMapper transactionMapper;
    private final ReconciliationMapper reconciliationMapper;

    // ==================== T5-1: Bank Account CRUD ====================

    public Page<AccountDTO> pageAccounts(int page, int size, String bankName, Integer status) {
        LambdaQueryWrapper<BankAccount> qw = new LambdaQueryWrapper<>();
        if (bankName != null && !bankName.isBlank()) qw.like(BankAccount::getBankName, bankName);
        if (status != null) qw.eq(BankAccount::getAccountStatus, status);
        qw.orderByDesc(BankAccount::getCreateTime);
        Page<BankAccount> pr = accountMapper.selectPage(new Page<>(page, size), qw);
        Page<AccountDTO> r = new Page<>(page, size, pr.getTotal());
        r.setRecords(pr.getRecords().stream().map(this::toAccountDTO).collect(Collectors.toList()));
        return r;
    }

    public AccountDTO getAccountById(Long id) {
        BankAccount a = accountMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "账户不存在");
        return toAccountDTO(a);
    }

    @Transactional
    public AccountDTO createAccount(AccountDTO dto) {
        BankAccount a = new BankAccount();
        a.setAccountNo(maskAccount(dto.getAccountNo()));
        a.setBankName(dto.getBankName());
        a.setAccountName(dto.getAccountName());
        a.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        a.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CNY");
        a.setAccountStatus(1);
        accountMapper.insert(a);
        log.info("银行账户创建: id={}, bankName={}, accountName={}", a.getId(), a.getBankName(), a.getAccountName());
        return toAccountDTO(a);
    }

    @Transactional
    public AccountDTO updateAccount(Long id, AccountDTO dto) {
        BankAccount a = accountMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "账户不存在");
        a.setBankName(dto.getBankName());
        a.setAccountName(dto.getAccountName());
        if (dto.getAccountNo() != null && !dto.getAccountNo().startsWith("***")) {
            a.setAccountNo(maskAccount(dto.getAccountNo()));
        }
        a.setBalance(dto.getBalance());
        a.setAccountStatus(dto.getAccountStatus());
        accountMapper.updateById(a);
        log.info("银行账户更新: id={}, bankName={}", id, a.getBankName());
        return toAccountDTO(a);
    }

    // ==================== T5-2: Fund Transactions ====================

    public Page<TransactionDTO> pageTransactions(int page, int size, Long accountId, Integer transType, String startDate, String endDate) {
        LambdaQueryWrapper<FundTransaction> qw = new LambdaQueryWrapper<>();
        if (accountId != null) qw.eq(FundTransaction::getAccountId, accountId);
        if (transType != null) qw.eq(FundTransaction::getTransType, transType);
        if (startDate != null) qw.ge(FundTransaction::getTransTime, startDate);
        if (endDate != null) qw.le(FundTransaction::getTransTime, endDate + " 23:59:59");
        qw.orderByDesc(FundTransaction::getTransTime);
        Page<FundTransaction> pr = transactionMapper.selectPage(new Page<>(page, size), qw);
        Page<TransactionDTO> r = new Page<>(page, size, pr.getTotal());
        r.setRecords(pr.getRecords().stream().map(this::toTransDTO).collect(Collectors.toList()));
        return r;
    }

    public TransactionDTO getTransactionById(Long id) {
        FundTransaction t = transactionMapper.selectById(id);
        if (t == null) throw new BusinessException(404, "流水不存在");
        return toTransDTO(t);
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO dto) {
        FundTransaction t = new FundTransaction();
        t.setTransNo("TXN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        t.setAccountId(dto.getAccountId());
        t.setTransType(dto.getTransType());
        t.setAmount(dto.getAmount());
        t.setCounterparty(dto.getCounterparty());
        t.setTransTime(dto.getTransTime() != null ? dto.getTransTime() : LocalDateTime.now());
        t.setSource(1); // manual
        t.setSummary(dto.getSummary());

        // update account balance
        BankAccount acct = accountMapper.selectById(dto.getAccountId());
        if (acct == null) throw new BusinessException(404, "账户不存在");
        if (dto.getTransType() == 1) acct.setBalance(acct.getBalance().add(dto.getAmount()));
        else if (dto.getTransType() == 2) acct.setBalance(acct.getBalance().subtract(dto.getAmount()));
        t.setBalanceAfter(acct.getBalance());
        accountMapper.updateById(acct);
        transactionMapper.insert(t);
        log.info("流水录入: id={}, transNo={}, accountId={}, type={}, amount={}", t.getId(), t.getTransNo(), dto.getAccountId(), dto.getTransType(), dto.getAmount());
        return toTransDTO(t);
    }

    // ==================== T5-3: Bank Sync (Mock) ====================

    @Transactional
    public Map<String, Object> batchSync(Long accountId) {
        BankAccount acct = accountMapper.selectById(accountId);
        if (acct == null) throw new BusinessException(404, "账户不存在");

        int imported = 0;
        // Mock: generate 3 sample bank transactions
        String[][] mockData = {{"1000.00", "客户付款"}, {"-500.00", "手续费"}, {"2000.00", "退款"}};
        for (String[] row : mockData) {
            FundTransaction t = new FundTransaction();
            t.setTransNo("BNK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "-" + (imported + 1));
            t.setAccountId(accountId);
            t.setTransType(row[0].startsWith("-") ? 2 : 1);
            t.setAmount(new BigDecimal(row[0].replace("-", "")));
            t.setCounterparty(row[1]);
            t.setTransTime(LocalDateTime.now().minusDays(imported));
            t.setSource(2); // bank sync
            t.setSummary("银行同步-" + row[1]);
            acct.setBalance(acct.getBalance().add(t.getAmount()));
            t.setBalanceAfter(acct.getBalance());
            transactionMapper.insert(t);
            imported++;
        }
        accountMapper.updateById(acct);
        log.info("银行同步完成: accountId={}, imported={}", accountId, imported);
        return Map.of("accountId", accountId, "importedCount", imported, "balance", acct.getBalance());
    }

    // ==================== T5-4: Reconciliation ====================

    public Page<ReconciliationDTO> pageReconciliations(int page, int size, Long accountId) {
        LambdaQueryWrapper<Reconciliation> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Reconciliation::getCreateTime);
        Page<Reconciliation> pr = reconciliationMapper.selectPage(new Page<>(page, size), qw);
        Page<ReconciliationDTO> r = new Page<>(page, size, pr.getTotal());
        r.setRecords(pr.getRecords().stream().map(this::toReconDTO).collect(Collectors.toList()));
        return r;
    }

    @Transactional
    public Map<String, Object> runReconciliation(ReconRunRequest req) {
        LambdaQueryWrapper<FundTransaction> qw = new LambdaQueryWrapper<>();
        qw.eq(FundTransaction::getAccountId, req.getAccountId());
        if (req.getStartDate() != null) qw.ge(FundTransaction::getTransTime, req.getStartDate());
        if (req.getEndDate() != null) qw.le(FundTransaction::getTransTime, req.getEndDate() + " 23:59:59");
        List<FundTransaction> sysTrans = transactionMapper.selectList(qw);

        int matched = 0, diffCount = 0;
        List<Map<String, Object>> diffs = new ArrayList<>();
        for (FundTransaction st : sysTrans) {
            Reconciliation recon = new Reconciliation();
            recon.setSystemTransId(st.getId());
            recon.setActualTransId("BANK-" + st.getTransNo());
            BigDecimal deviation = new BigDecimal(Math.random() < 0.3 ? "0.5" : "0");
            BigDecimal bankAmount = st.getAmount().add(deviation);
            BigDecimal diff = st.getAmount().subtract(bankAmount).abs();
            recon.setDiffAmount(diff);
            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                recon.setDiffType(1); // matched
                recon.setHandleStatus(1);
                matched++;
            } else {
                recon.setDiffType(2); // AMOUNT_MISMATCH
                recon.setHandleStatus(0); // pending
                recon.setRemark("{\"type\":\"AMOUNT_MISMATCH\",\"sysAmount\":" + st.getAmount() + ",\"bankAmount\":" + bankAmount + "}");
                diffs.add(Map.of("type", "AMOUNT_MISMATCH", "transNo", st.getTransNo(), "sysAmount", st.getAmount(), "bankAmount", bankAmount, "diffAmount", diff));
                diffCount++;
            }
            reconciliationMapper.insert(recon);
        }
        log.info("对账完成: accountId={}, total={}, matched={}, diffs={}", req.getAccountId(), sysTrans.size(), matched, diffCount);
        return Map.of("accountId", req.getAccountId(), "totalCount", sysTrans.size(), "matchedCount", matched, "diffCount", diffCount, "diffs", diffs);
    }

    public Page<ReconciliationDTO> pageDiffs(int page, int size) {
        LambdaQueryWrapper<Reconciliation> qw = new LambdaQueryWrapper<>();
        qw.eq(Reconciliation::getHandleStatus, 0).orderByDesc(Reconciliation::getCreateTime);
        Page<Reconciliation> pr = reconciliationMapper.selectPage(new Page<>(page, size), qw);
        Page<ReconciliationDTO> r = new Page<>(page, size, pr.getTotal());
        r.setRecords(pr.getRecords().stream().map(this::toReconDTO).collect(Collectors.toList()));
        return r;
    }

    @Transactional
    public void handleReconciliation(Long id, String action) {
        Reconciliation recon = reconciliationMapper.selectById(id);
        if (recon == null) throw new BusinessException(404, "对账记录不存在");
        recon.setHandleStatus("APPROVE".equals(action) ? 1 : 2); // 1=已处理/接受, 2=已忽略
        recon.setHandleTime(LocalDateTime.now());
        reconciliationMapper.updateById(recon);
        log.info("对账差异处理: id={}, action={}", id, action);
    }

    // ==================== Helpers ====================

    private String maskAccount(String raw) {
        if (raw == null || raw.length() <= 4) return raw;
        return "***" + raw.substring(raw.length() - 4);
    }

    private AccountDTO toAccountDTO(BankAccount a) {
        AccountDTO d = new AccountDTO();
        d.setId(a.getId()); d.setAccountNo(a.getAccountNo()); d.setBankName(a.getBankName());
        d.setAccountName(a.getAccountName()); d.setBalance(a.getBalance()); d.setCurrency(a.getCurrency());
        d.setAccountStatus(a.getAccountStatus());
        d.setAccountStatusDesc(a.getAccountStatus() == 1 ? "正常" : "停用");
        d.setCreateTime(a.getCreateTime()); d.setUpdateTime(a.getUpdateTime());
        return d;
    }

    private TransactionDTO toTransDTO(FundTransaction t) {
        TransactionDTO d = new TransactionDTO();
        d.setId(t.getId()); d.setTransNo(t.getTransNo()); d.setAccountId(t.getAccountId());
        d.setTransType(t.getTransType());
        d.setTransTypeDesc(t.getTransType() == 1 ? "收入" : t.getTransType() == 2 ? "支出" : "转账");
        d.setAmount(t.getAmount()); d.setBalanceAfter(t.getBalanceAfter());
        d.setCounterparty(t.getCounterparty()); d.setTransTime(t.getTransTime());
        d.setSource(t.getSource()); d.setSourceDesc(t.getSource() == 1 ? "手动录入" : "银行同步");
        d.setSummary(t.getSummary()); d.setCreateTime(t.getCreateTime());
        return d;
    }

    private ReconciliationDTO toReconDTO(Reconciliation r) {
        ReconciliationDTO d = new ReconciliationDTO();
        d.setId(r.getId());
        d.setDiffAmount(r.getDiffAmount());
        d.setMatchStatus(r.getHandleStatus());
        d.setMatchStatusDesc(r.getHandleStatus() == 1 ? "已处理" : r.getHandleStatus() == 2 ? "已忽略" : "待处理");
        d.setDetail(r.getRemark()); d.setCreateTime(r.getCreateTime());
        return d;
    }
}
