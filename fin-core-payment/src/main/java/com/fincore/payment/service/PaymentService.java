package com.fincore.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.exception.BusinessException;
import com.fincore.payment.dto.PaymentDTO;
import com.fincore.payment.entity.ApprovalFlow;
import com.fincore.payment.entity.PaymentRecord;
import com.fincore.payment.mapper.ApprovalFlowMapper;
import com.fincore.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final ApprovalFlowMapper approvalFlowMapper;
    private final ApprovalService approvalService;

    public Page<PaymentDTO> page(int page, int size, String payeeName, Integer status,
                                  String startDate, String endDate) {
        LambdaQueryWrapper<PaymentRecord> qw = new LambdaQueryWrapper<>();
        if (payeeName != null && !payeeName.isBlank()) qw.like(PaymentRecord::getPayeeName, payeeName);
        if (status != null) qw.eq(PaymentRecord::getPaymentStatus, status);
        if (startDate != null) qw.ge(PaymentRecord::getCreateTime, startDate);
        if (endDate != null) qw.le(PaymentRecord::getCreateTime, endDate + " 23:59:59");
        qw.orderByDesc(PaymentRecord::getCreateTime);

        Page<PaymentRecord> pr = paymentMapper.selectPage(new Page<>(page, size), qw);
        Page<PaymentDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return result;
    }

    public PaymentDTO getById(Long id) {
        PaymentRecord p = paymentMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "付款记录不存在");
        return toDTO(p);
    }

    @Transactional
    public PaymentDTO apply(PaymentDTO dto) {
        PaymentRecord p = new PaymentRecord();
        p.setPaymentNo("PAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        p.setPayeeName(dto.getPayeeName());
        p.setAmount(dto.getAmount());
        p.setSupplierId(dto.getSupplierId());
        p.setOrderId(dto.getOrderId());
        p.setPaymentStatus(1); // 待审批
        p.setAppliedBy(1L); // TODO: 从认证上下文获取
        p.setAppliedTime(LocalDateTime.now());
        paymentMapper.insert(p);
        log.info("付款申请已提交: id={}, paymentNo={}, payee={}, amount={}", p.getId(), p.getPaymentNo(), p.getPayeeName(), p.getAmount());

        // 创建审批流
        approvalService.createApprovalFlow("PAYMENT", p.getId());

        return toDTO(p);
    }

    @Transactional
    public PaymentDTO approve(Long id, String comment, Long approvedBy) {
        PaymentRecord p = paymentMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "付款记录不存在");
        if (p.getPaymentStatus() != 1 && p.getPaymentStatus() != 2) {
            throw new BusinessException(409, "仅待审批/审批中的付款可审批");
        }

        // 处理审批节点
        ApprovalFlow flow = approvalFlowMapper.selectOne(
                new LambdaQueryWrapper<ApprovalFlow>()
                        .eq(ApprovalFlow::getBizType, "PAYMENT")
                        .eq(ApprovalFlow::getBizId, id)
                        .eq(ApprovalFlow::getApprovalStatus, 1));
        if (flow == null) {
            // 审批流已完成/不存在，但付款仍在审批中 → 直接通过
            p.setPaymentStatus(3);
            paymentMapper.updateById(p);
            return toDTO(p);
        }

        approvalService.processNode(flow, "APPROVE", comment, approvedBy);

        // 审批完成 → 更新付款状态
        ApprovalFlow updatedFlow = approvalFlowMapper.selectById(flow.getId());
        if (updatedFlow.getApprovalStatus() == 2) {
            p.setPaymentStatus(3); // 已付款
        } else {
            p.setPaymentStatus(2); // 审批中
        }
        paymentMapper.updateById(p);

        return toDTO(p);
    }

    @Transactional
    public PaymentDTO reject(Long id, String comment, Long approvedBy) {
        PaymentRecord p = paymentMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "付款记录不存在");
        if (p.getPaymentStatus() != 1 && p.getPaymentStatus() != 2) {
            throw new BusinessException(409, "仅待审批/审批中的付款可驳回");
        }

        ApprovalFlow flow = approvalFlowMapper.selectOne(
                new LambdaQueryWrapper<ApprovalFlow>()
                        .eq(ApprovalFlow::getBizType, "PAYMENT")
                        .eq(ApprovalFlow::getBizId, id)
                        .eq(ApprovalFlow::getApprovalStatus, 1));
        if (flow == null) throw new BusinessException(404, "审批流程不存在");

        approvalService.processNode(flow, "REJECT", comment, approvedBy);

        p.setPaymentStatus(4); // 已驳回
        paymentMapper.updateById(p);

        return toDTO(p);
    }

    private PaymentDTO toDTO(PaymentRecord p) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setPaymentNo(p.getPaymentNo());
        dto.setPayeeName(p.getPayeeName());
        dto.setAmount(p.getAmount());
        dto.setSupplierId(p.getSupplierId());
        dto.setOrderId(p.getOrderId());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setPaymentStatusDesc(switch (p.getPaymentStatus()) {
            case 1 -> "待审批";
            case 2 -> "审批中";
            case 3 -> "已付款";
            case 4 -> "已驳回";
            default -> "未知";
        });
        dto.setAppliedBy(p.getAppliedBy());
        dto.setAppliedTime(p.getAppliedTime());
        dto.setCreateTime(p.getCreateTime());
        dto.setUpdateTime(p.getUpdateTime());
        return dto;
    }
}
