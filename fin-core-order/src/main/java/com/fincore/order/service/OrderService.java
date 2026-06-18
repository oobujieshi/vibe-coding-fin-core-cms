package com.fincore.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.exception.BusinessException;
import com.fincore.order.dto.*;
import com.fincore.order.entity.*;
import com.fincore.order.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final FeeRuleMapper feeRuleMapper;
    private final SettlementMapper settlementMapper;

    // ========== Order CRUD ==========

    public Page<OrderDTO> page(int page, int size, String customerName, Integer status,
                                String startDate, String endDate) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        if (customerName != null && !customerName.isBlank()) qw.like(Order::getCustomerName, customerName);
        if (status != null) qw.eq(Order::getOrderStatus, status);
        if (startDate != null) qw.ge(Order::getCreateTime, startDate);
        if (endDate != null) qw.le(Order::getCreateTime, endDate + " 23:59:59");
        qw.orderByDesc(Order::getCreateTime);

        Page<Order> pr = orderMapper.selectPage(new Page<>(page, size), qw);
        Page<OrderDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return result;
    }

    public OrderDTO getById(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        return toDTO(o);
    }

    @Transactional
    public OrderDTO create(OrderDTO dto) {
        Order o = new Order();
        o.setOrderNo(generateOrderNo());
        o.setCustomerName(dto.getCustomerName());
        o.setContractNo(dto.getContractNo());
        o.setTotalAmount(dto.getTotalAmount());
        o.setOrderStatus(1);
        o.setRemark(dto.getRemark());
        orderMapper.insert(o);
        log.info("订单创建成功: id={}, orderNo={}, customerName={}, amount={}", o.getId(), o.getOrderNo(), o.getCustomerName(), o.getTotalAmount());

        if (dto.getItems() != null) {
            for (OrderItemDTO idto : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(o.getId());
                item.setProductName(idto.getProductName());
                item.setQuantity(idto.getQuantity());
                item.setUnitPrice(idto.getUnitPrice());
                item.setAmount(idto.getAmount());
                orderItemMapper.insert(item);
            }
        }
        return toDTO(o);
    }

    @Transactional
    public OrderDTO update(Long id, OrderDTO dto) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (o.getOrderStatus() != 1) throw new BusinessException(409, "仅待结算订单可修改");

        o.setCustomerName(dto.getCustomerName());
        o.setContractNo(dto.getContractNo());
        o.setTotalAmount(dto.getTotalAmount());
        o.setRemark(dto.getRemark());
        orderMapper.updateById(o);
        log.info("订单更新成功: id={}, orderNo={}, amount={}", o.getId(), o.getOrderNo(), o.getTotalAmount());

        if (dto.getItems() != null) {
            orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
            for (OrderItemDTO idto : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(id);
                item.setProductName(idto.getProductName());
                item.setQuantity(idto.getQuantity());
                item.setUnitPrice(idto.getUnitPrice());
                item.setAmount(idto.getAmount());
                orderItemMapper.insert(item);
            }
        }
        return toDTO(o);
    }

    @Transactional
    public void delete(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (o.getOrderStatus() != 1) throw new BusinessException(409, "仅待结算订单可关闭");
        o.setOrderStatus(3);
        orderMapper.updateById(o);
        log.info("订单已关闭: id={}, orderNo={}", id, o.getOrderNo());
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException(404, "订单不存在");
        int from = o.getOrderStatus();
        if (from == 1 && status == 2) {
            long cnt = settlementMapper.selectCount(new LambdaQueryWrapper<Settlement>().eq(Settlement::getOrderId, id));
            if (cnt == 0) throw new BusinessException(409, "请先生成结算单");
        }
        if (from == 1 && status == 3) {
            // 可直接关闭
            o.setOrderStatus(3);
            orderMapper.updateById(o);
            return;
        }
        o.setOrderStatus(status);
        orderMapper.updateById(o);
    }

    // ========== Batch Calculation ==========

    @Transactional
    public BatchCalcResult batchCalc(BatchCalcRequest req) {
        List<FeeRule> rules = feeRuleMapper.selectList(
                new LambdaQueryWrapper<FeeRule>().eq(FeeRule::getIsActive, 1).orderByAsc(FeeRule::getId));
        if (rules.isEmpty()) throw new BusinessException(400, "没有可用的费率规则");

        List<BatchCalcResult.CalcItem> results = new ArrayList<>();
        int success = 0, fail = 0;
        for (Long oid : req.getOrderIds()) {
            Order o = orderMapper.selectById(oid);
            if (o == null) {
                results.add(buildFailed(oid, null, "订单不存在"));
                fail++; continue;
            }
            if (o.getOrderStatus() != 1) {
                results.add(buildFailed(oid, o.getOrderNo(), "仅待结算订单可计算"));
                fail++; continue;
            }
            try {
                FeeRule rule = rules.get(0); // simplest: first active rule
                BigDecimal fee = o.getTotalAmount().multiply(parseRate(rule.getRuleConfig()));
                BigDecimal real = o.getTotalAmount().subtract(fee);

                o.setCalculatedAmount(real);
                o.setFeeAmount(fee);
                o.setFeeDetail("{\"ruleId\":" + rule.getId() + ",\"ruleName\":\"" + rule.getRuleName() + "\"}");
                orderMapper.updateById(o);

                BatchCalcResult.CalcItem item = new BatchCalcResult.CalcItem();
                item.setOrderId(oid); item.setOrderNo(o.getOrderNo());
                item.setSuccess(true);
                item.setOriginalAmount(o.getTotalAmount());
                item.setCalculatedAmount(real);
                Map<String,Object> fd = new HashMap<>(); fd.put("ruleId", rule.getId()); fd.put("ruleName", rule.getRuleName()); fd.put("rate", parseRate(rule.getRuleConfig()));
                fd.put("feeAmount", fee);
                item.setFeeDetail(fd);
                results.add(item);
                success++;
            } catch (Exception e) {
                results.add(buildFailed(oid, o.getOrderNo(), e.getMessage()));
                fail++;
            }
        }
        BatchCalcResult r = new BatchCalcResult();
        r.setTotalCount(req.getOrderIds().size()); r.setSuccessCount(success); r.setFailCount(fail); r.setResults(results);
        log.info("批量计算完成: total={}, success={}, fail={}", req.getOrderIds().size(), success, fail);
        return r;
    }

    // ========== Settlement ==========

    public Page<SettlementDTO> pageSettlements(int page, int size, Long orderId, Integer status, String startDate, String endDate) {
        LambdaQueryWrapper<Settlement> qw = new LambdaQueryWrapper<>();
        if (orderId != null) qw.eq(Settlement::getOrderId, orderId);
        if (status != null) qw.eq(Settlement::getSettlementStatus, status);
        if (startDate != null) qw.ge(Settlement::getCreateTime, startDate);
        if (endDate != null) qw.le(Settlement::getCreateTime, endDate + " 23:59:59");
        qw.orderByDesc(Settlement::getCreateTime);

        Page<Settlement> pr = settlementMapper.selectPage(new Page<>(page, size), qw);
        Page<SettlementDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toSettlementDTO).collect(Collectors.toList()));
        return result;
    }

    public SettlementDTO getSettlementById(Long id) {
        Settlement s = settlementMapper.selectById(id);
        if (s == null) throw new BusinessException(404, "结算单不存在");
        return toSettlementDTO(s);
    }

    @Transactional
    public List<SettlementDTO> createSettlements(List<Long> orderIds, String remark) {
        List<SettlementDTO> list = new ArrayList<>();
        for (Long oid : orderIds) {
            Order o = orderMapper.selectById(oid);
            if (o == null || o.getOrderStatus() != 1) continue;
            if (o.getCalculatedAmount() == null) continue; // not calculated yet

            Settlement s = new Settlement();
            s.setSettlementNo("STL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + oid);
            s.setOrderId(oid);
            s.setCalculatedAmount(o.getCalculatedAmount());
            s.setFeeDetail(o.getFeeDetail());
            s.setSettlementStatus(1);
            settlementMapper.insert(s);

            o.setOrderStatus(2);
            orderMapper.updateById(o);
            list.add(toSettlementDTO(s));
        }
        log.info("结算单生成完成: count={}", list.size());
        return list;
    }

    @Transactional
    public void confirmSettlement(Long id) {
        Settlement s = settlementMapper.selectById(id);
        if (s == null) throw new BusinessException(404, "结算单不存在");
        if (s.getSettlementStatus() != 1) throw new BusinessException(409, "仅待确认结算单可确认");
        s.setSettlementStatus(2);
        s.setSettledTime(LocalDateTime.now());
        settlementMapper.updateById(s);
        log.info("结算单已确认: id={}, settlementNo={}", id, s.getSettlementNo());
    }

    @Transactional
    public void cancelSettlement(Long id) {
        Settlement s = settlementMapper.selectById(id);
        if (s == null) throw new BusinessException(404, "结算单不存在");
        if (s.getSettlementStatus() != 1) throw new BusinessException(409, "仅待确认结算单可作废");
        s.setSettlementStatus(3);
        settlementMapper.updateById(s);
        // revert order back
        Order o = orderMapper.selectById(s.getOrderId());
        if (o != null) {
            o.setOrderStatus(1);
            orderMapper.updateById(o);
        }
        log.info("结算单已作废: id={}, settlementNo={}, 回退订单{}至待结算", id, s.getSettlementNo(), s.getOrderId());
    }

    // ========== Fee Rules ==========

    public Page<FeeRuleDTO> pageFeeRules(int page, int size, String ruleName, Integer status) {
        LambdaQueryWrapper<FeeRule> qw = new LambdaQueryWrapper<>();
        if (ruleName != null && !ruleName.isBlank()) qw.like(FeeRule::getRuleName, ruleName);
        if (status != null) qw.eq(FeeRule::getIsActive, status);
        qw.orderByAsc(FeeRule::getId);
        Page<FeeRule> pr = feeRuleMapper.selectPage(new Page<>(page, size), qw);
        Page<FeeRuleDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toFeeRuleDTO).collect(Collectors.toList()));
        return result;
    }

    public FeeRuleDTO createFeeRule(FeeRuleDTO dto) {
        FeeRule r = new FeeRule();
        r.setRuleName(dto.getRuleName());
        r.setRuleType("PERCENTAGE".equals(dto.getRuleType()) ? 1 : 2);
        r.setRuleConfig(toJsonConfig(dto));
        r.setIsActive(1);
        feeRuleMapper.insert(r);
        log.info("费率规则创建: id={}, name={}, type={}, rate={}", r.getId(), r.getRuleName(), dto.getRuleType(), dto.getRate());
        return toFeeRuleDTO(r);
    }

    public FeeRuleDTO updateFeeRule(Long id, FeeRuleDTO dto) {
        FeeRule r = feeRuleMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "规则不存在");
        r.setRuleName(dto.getRuleName());
        r.setRuleType("PERCENTAGE".equals(dto.getRuleType()) ? 1 : 2);
        r.setRuleConfig(toJsonConfig(dto));
        feeRuleMapper.updateById(r);
        return toFeeRuleDTO(r);
    }

    public void toggleFeeRule(Long id, Integer status) {
        FeeRule r = feeRuleMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "规则不存在");
        r.setIsActive(status);
        feeRuleMapper.updateById(r);
        log.info("费率规则{}: id={}, name={}, status={}", status == 1 ? "启用" : "禁用", id, r.getRuleName(), status);
    }

    // ========== Helper methods ==========

    private OrderDTO toDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId()); dto.setOrderNo(o.getOrderNo());
        dto.setCustomerName(o.getCustomerName()); dto.setCustomerId(o.getCustomerId());
        dto.setContractNo(o.getContractNo()); dto.setTotalAmount(o.getTotalAmount());
        dto.setCalculatedAmount(o.getCalculatedAmount()); dto.setFeeAmount(o.getFeeAmount());
        dto.setFeeDetail(o.getFeeDetail()); dto.setStatus(o.getOrderStatus());
        dto.setRemark(o.getRemark()); dto.setCreateTime(o.getCreateTime()); dto.setUpdateTime(o.getUpdateTime());
        // load items
        if (o.getId() != null) {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            dto.setItems(items.stream().map(i -> {
                OrderItemDTO idto = new OrderItemDTO();
                idto.setId(i.getId()); idto.setOrderId(i.getOrderId());
                idto.setProductName(i.getProductName()); idto.setQuantity(i.getQuantity());
                idto.setUnitPrice(i.getUnitPrice()); idto.setAmount(i.getAmount());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private SettlementDTO toSettlementDTO(Settlement s) {
        SettlementDTO dto = new SettlementDTO();
        dto.setId(s.getId()); dto.setSettlementNo(s.getSettlementNo()); dto.setOrderId(s.getOrderId());
        dto.setRealAmount(s.getCalculatedAmount());
        dto.setStatus(s.getSettlementStatus()); dto.setCreateTime(s.getCreateTime()); dto.setUpdateTime(s.getUpdateTime());
        if (s.getOrderId() != null) {
            Order o = orderMapper.selectById(s.getOrderId());
            if (o != null) { dto.setOrderNo(o.getOrderNo()); dto.setCustomerName(o.getCustomerName()); dto.setOriginalAmount(o.getTotalAmount()); }
        }
        return dto;
    }

    private FeeRuleDTO toFeeRuleDTO(FeeRule r) {
        FeeRuleDTO dto = new FeeRuleDTO();
        dto.setId(r.getId()); dto.setRuleName(r.getRuleName());
        dto.setRuleType(r.getRuleType() == 1 ? "PERCENTAGE" : "FIXED");
        dto.setStatus(r.getIsActive()); dto.setCreateTime(r.getCreateTime()); dto.setUpdateTime(r.getUpdateTime());
        if (r.getRuleConfig() != null) {
            String[] parts = r.getRuleConfig().split(";");
            for (String p : parts) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2) {
                    if ("rate".equals(kv[0])) dto.setRate(new BigDecimal(kv[1]));
                    if ("minFee".equals(kv[0])) dto.setMinFee(new BigDecimal(kv[1]));
                    if ("maxFee".equals(kv[0])) dto.setMaxFee(new BigDecimal(kv[1]));
                }
            }
        }
        return dto;
    }

    private String toJsonConfig(FeeRuleDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getRate() != null) sb.append("rate=").append(dto.getRate()).append(";");
        if (dto.getMinFee() != null) sb.append("minFee=").append(dto.getMinFee()).append(";");
        if (dto.getMaxFee() != null) sb.append("maxFee=").append(dto.getMaxFee()).append(";");
        return sb.toString();
    }

    private BigDecimal parseRate(String config) {
        if (config == null) return BigDecimal.ZERO;
        for (String p : config.split(";")) {
            if (p.startsWith("rate=")) return new BigDecimal(p.substring(5));
        }
        return BigDecimal.ZERO;
    }

    private String generateOrderNo() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private BatchCalcResult.CalcItem buildFailed(Long oid, String no, String msg) {
        BatchCalcResult.CalcItem item = new BatchCalcResult.CalcItem();
        item.setOrderId(oid); item.setOrderNo(no); item.setSuccess(false); item.setErrorMsg(msg);
        return item;
    }
}
