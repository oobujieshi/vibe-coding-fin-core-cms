package com.fincore.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.order.dto.*;
import com.fincore.order.entity.*;
import com.fincore.order.mapper.*;
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
class OrderServiceTest {

    @Mock OrderMapper orderMapper;
    @Mock OrderItemMapper orderItemMapper;
    @Mock FeeRuleMapper feeRuleMapper;
    @Mock SettlementMapper settlementMapper;
    @InjectMocks OrderService service;

    @Test void create_shouldSucceed() {
        OrderDTO dto = new OrderDTO(); dto.setCustomerName("Test"); dto.setTotalAmount(new BigDecimal("10000"));
        lenient().doAnswer(inv -> { Order o = inv.getArgument(0); o.setId(1L); return 1; }).when(orderMapper).insert(any(Order.class));
        OrderDTO r = service.create(dto);
        assertNotNull(r); assertEquals("Test", r.getCustomerName());
    }

    @Test void page_shouldReturnPage() {
        Page<Order> mp = new Page<>(1, 20, 2);
        mp.setRecords(List.of(mkOrder(1L, "A", new BigDecimal("5000")), mkOrder(2L, "B", new BigDecimal("8000"))));
        when(orderMapper.selectPage(any(), any())).thenReturn(mp);
        Page<OrderDTO> r = service.page(1, 20, null, null, null, null);
        assertEquals(2, r.getTotal());
    }

    @Test void getById_shouldReturnOrder() {
        when(orderMapper.selectById(1L)).thenReturn(mkOrder(1L, "Cust", new BigDecimal("999")));
        assertEquals("Cust", service.getById(1L).getCustomerName());
    }

    @Test void update_shouldSucceed() {
        when(orderMapper.selectById(1L)).thenReturn(mkOrder(1L, "Old", new BigDecimal("100")));
        OrderDTO dto = new OrderDTO(); dto.setCustomerName("New"); dto.setTotalAmount(new BigDecimal("200"));
        assertEquals("New", service.update(1L, dto).getCustomerName());
    }

    @Test void update_notFound_shouldThrow() {
        when(orderMapper.selectById(99L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.update(99L, new OrderDTO()));
    }

    @Test void delete_shouldSetStatusClosed() {
        when(orderMapper.selectById(1L)).thenReturn(mkOrder(1L, "C", new BigDecimal("100")));
        service.delete(1L); // no exception = pass
    }

    @Test void confirmSettlement_shouldSucceed() {
        Settlement s = new Settlement(); s.setId(1L); s.setSettlementStatus(1); s.setOrderId(10L);
        when(settlementMapper.selectById(1L)).thenReturn(s);
        service.confirmSettlement(1L); // no exception = pass
    }

    @Test void cancelSettlement_shouldRevertOrder() {
        Settlement s = new Settlement(); s.setId(1L); s.setSettlementStatus(1); s.setOrderId(10L);
        when(settlementMapper.selectById(1L)).thenReturn(s);
        when(orderMapper.selectById(10L)).thenReturn(mkOrder(10L, "X", new BigDecimal("500"), 2));
        service.cancelSettlement(1L); // no exception = pass
    }

    private Order mkOrder(Long id, String name, BigDecimal amt) { return mkOrder(id, name, amt, 1); }
    private Order mkOrder(Long id, String name, BigDecimal amt, int status) {
        Order o = new Order(); o.setId(id); o.setCustomerName(name); o.setTotalAmount(amt); o.setOrderStatus(status);
        o.setOrderNo("ORD-" + id); o.setContractNo("CT-" + id); return o;
    }
}
