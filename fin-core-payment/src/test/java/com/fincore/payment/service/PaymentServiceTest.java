package com.fincore.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.payment.dto.PaymentDTO;
import com.fincore.payment.entity.*;
import com.fincore.payment.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock PaymentMapper paymentMapper;
    @Mock ApprovalFlowMapper approvalFlowMapper;
    @Mock ApprovalService approvalService;
    @InjectMocks PaymentService service;

    @Test void apply_shouldCreatePayment() {
        lenient().doAnswer(inv -> { PaymentRecord p = inv.getArgument(0); p.setId(1L); return 1; }).when(paymentMapper).insert(any(PaymentRecord.class));
        PaymentDTO dto = new PaymentDTO(); dto.setPayeeName("Supplier"); dto.setAmount(new BigDecimal("5000"));
        assertEquals("Supplier", service.apply(dto).getPayeeName());
    }

    @Test void page_shouldReturnList() {
        Page<PaymentRecord> mp = new Page<>(1, 20, 1);
        PaymentRecord p = new PaymentRecord(); p.setId(1L); p.setPayeeName("S"); p.setPaymentNo("PAY-001"); p.setAmount(new BigDecimal("1000")); p.setPaymentStatus(1);
        mp.setRecords(List.of(p));
        when(paymentMapper.selectPage(any(), any())).thenReturn(mp);
        assertEquals(1, service.page(1, 20, null, null, null, null).getTotal());
    }

    @Test void approve_shouldPass() {
        PaymentRecord p = new PaymentRecord(); p.setId(1L); p.setPaymentStatus(1); p.setPaymentNo("P"); p.setAmount(new BigDecimal("100"));
        when(paymentMapper.selectById(1L)).thenReturn(p);
        ApprovalFlow flow = new ApprovalFlow(); flow.setId(10L);
        when(approvalFlowMapper.selectOne(any())).thenReturn(flow);
        when(approvalFlowMapper.selectById(10L)).thenReturn(flow);
        flow.setApprovalStatus(2);
        assertNotNull(service.approve(1L, "OK", 1L));
    }

    @Test void approve_wrongStatus_shouldThrow() {
        PaymentRecord p = new PaymentRecord(); p.setId(1L); p.setPaymentStatus(3);
        when(paymentMapper.selectById(1L)).thenReturn(p);
        assertThrows(RuntimeException.class, () -> service.approve(1L, "OK", 1L));
    }

    @Test void approve_noFlow_shouldPassDirectly() {
        PaymentRecord p = new PaymentRecord(); p.setId(1L); p.setPaymentStatus(2); p.setPaymentNo("P"); p.setAmount(new BigDecimal("100"));
        when(paymentMapper.selectById(1L)).thenReturn(p);
        when(approvalFlowMapper.selectOne(any())).thenReturn(null);
        assertEquals(3, service.approve(1L, "OK", 1L).getPaymentStatus());
    }

    @Test void reject_shouldReject() {
        PaymentRecord p = new PaymentRecord(); p.setId(1L); p.setPaymentStatus(1); p.setPaymentNo("P"); p.setAmount(new BigDecimal("100"));
        when(paymentMapper.selectById(1L)).thenReturn(p);
        ApprovalFlow flow = new ApprovalFlow(); flow.setId(10L);
        when(approvalFlowMapper.selectOne(any())).thenReturn(flow);
        assertNotNull(service.reject(1L, "Bad", 1L));
    }

    @Test void reject_wrongStatus_shouldThrow() {
        PaymentRecord p = new PaymentRecord(); p.setId(1L); p.setPaymentStatus(3);
        when(paymentMapper.selectById(1L)).thenReturn(p);
        assertThrows(RuntimeException.class, () -> service.reject(1L, "Bad", 1L));
    }
}
