package com.fincore.fund.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.fund.dto.*;
import com.fincore.fund.entity.*;
import com.fincore.fund.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

    @Mock BankAccountMapper accountMapper;
    @Mock FundTransactionMapper transactionMapper;
    @Mock ReconciliationMapper reconciliationMapper;
    @InjectMocks FundService service;

    @Test void pageAccounts_shouldReturnPage() {
        Page<BankAccount> mp = new Page<>(1, 20, 2);
        BankAccount a1 = new BankAccount(); a1.setId(1L); a1.setBankName("B1"); a1.setAccountName("A1"); a1.setBalance(new BigDecimal("100")); a1.setAccountStatus(1);
        BankAccount a2 = new BankAccount(); a2.setId(2L); a2.setBankName("B2"); a2.setAccountName("A2"); a2.setBalance(new BigDecimal("200")); a2.setAccountStatus(1);
        mp.setRecords(List.of(a1, a2));
        when(accountMapper.selectPage(any(), any())).thenReturn(mp);
        assertEquals(2, service.pageAccounts(1, 20, null, null).getTotal());
    }

    @Test void handleReconciliation_shouldSucceed() {
        Reconciliation r = new Reconciliation(); r.setId(1L); r.setDiffAmount(new BigDecimal("100")); r.setHandleStatus(0);
        when(reconciliationMapper.selectById(1L)).thenReturn(r);
        service.handleReconciliation(1L, "APPROVE"); // no exception = pass
    }

    @Test void handleReconciliation_notFound_shouldThrow() {
        when(reconciliationMapper.selectById(99L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.handleReconciliation(99L, "APPROVE"));
    }
}
