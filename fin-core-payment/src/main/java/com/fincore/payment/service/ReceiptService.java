package com.fincore.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.exception.BusinessException;
import com.fincore.payment.dto.ReceiptDTO;
import com.fincore.payment.entity.Receipt;
import com.fincore.payment.mapper.ReceiptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptMapper receiptMapper;

    public Page<ReceiptDTO> page(int page, int size, String payerName, Integer status,
                                  Long orderId, String startDate, String endDate) {
        LambdaQueryWrapper<Receipt> qw = new LambdaQueryWrapper<>();
        if (payerName != null && !payerName.isBlank()) qw.like(Receipt::getPayerName, payerName);
        if (status != null) qw.eq(Receipt::getReceiptStatus, status);
        if (orderId != null) qw.eq(Receipt::getOrderId, orderId);
        if (startDate != null) qw.ge(Receipt::getCreateTime, startDate);
        if (endDate != null) qw.le(Receipt::getCreateTime, endDate + " 23:59:59");
        qw.orderByDesc(Receipt::getCreateTime);

        Page<Receipt> pr = receiptMapper.selectPage(new Page<>(page, size), qw);
        Page<ReceiptDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return result;
    }

    public ReceiptDTO getById(Long id) {
        Receipt r = receiptMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "收款记录不存在");
        return toDTO(r);
    }

    @Transactional
    public ReceiptDTO create(ReceiptDTO dto) {
        Receipt r = new Receipt();
        r.setReceiptNo("RCP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        r.setPayerName(dto.getPayerName());
        r.setAmount(dto.getAmount());
        r.setOrderId(dto.getOrderId());
        r.setBillId(dto.getBillId());
        r.setReceiptMethod(dto.getReceiptMethod() != null ? dto.getReceiptMethod() : 1);
        r.setReceiptStatus(1); // 待确认
        receiptMapper.insert(r);
        log.info("收款记录创建: id={}, receiptNo={}, payer={}, amount={}", r.getId(), r.getReceiptNo(), r.getPayerName(), r.getAmount());
        return toDTO(r);
    }

    @Transactional
    public ReceiptDTO confirm(Long id) {
        Receipt r = receiptMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "收款记录不存在");
        if (r.getReceiptStatus() != 1) throw new BusinessException(409, "仅待确认的收款可确认到账");
        r.setReceiptStatus(2);
        r.setConfirmedTime(LocalDateTime.now());
        receiptMapper.updateById(r);
        log.info("收款已确认到账: id={}, receiptNo={}, amount={}", r.getId(), r.getReceiptNo(), r.getAmount());
        return toDTO(r);
    }

    @Transactional
    public ReceiptDTO verifyByScan(String verifyCode) {
        LambdaQueryWrapper<Receipt> qw = new LambdaQueryWrapper<>();
        qw.eq(Receipt::getReceiptStatus, 1);
        qw.orderByDesc(Receipt::getCreateTime);
        List<Receipt> receipts = receiptMapper.selectList(qw);

        // 生成核销码匹配逻辑：hash receiptNo 取后8位
        for (Receipt r : receipts) {
            String expectedCode = generateVerifyCode(r.getReceiptNo());
            if (expectedCode.equals(verifyCode)) {
                r.setReceiptStatus(3); // 已核销
                r.setConfirmedTime(LocalDateTime.now());
                receiptMapper.updateById(r);
                log.info("收款已核销: id={}, receiptNo={}, verifyCode={}", r.getId(), r.getReceiptNo(), verifyCode);
                return toDTO(r);
            }
        }
        throw new BusinessException(404, "核销码无效或收款已处理");
    }

    private String generateVerifyCode(String receiptNo) {
        String base = receiptNo + "FIN_SALT";
        int hash = base.hashCode();
        return String.format("%06d", Math.abs(hash) % 1000000);
    }

    public String getVerifyCode(Long receiptId) {
        Receipt r = receiptMapper.selectById(receiptId);
        if (r == null) throw new BusinessException(404, "收款记录不存在");
        return generateVerifyCode(r.getReceiptNo());
    }

    private ReceiptDTO toDTO(Receipt r) {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setId(r.getId());
        dto.setReceiptNo(r.getReceiptNo());
        dto.setPayerName(r.getPayerName());
        dto.setAmount(r.getAmount());
        dto.setOrderId(r.getOrderId());
        dto.setBillId(r.getBillId());
        dto.setReceiptMethod(r.getReceiptMethod());
        dto.setReceiptMethodDesc(getMethodDesc(r.getReceiptMethod()));
        dto.setReceiptStatus(r.getReceiptStatus());
        dto.setReceiptStatusDesc(getStatusDesc(r.getReceiptStatus()));
        dto.setConfirmedTime(r.getConfirmedTime());
        dto.setVerifyCode(generateVerifyCode(r.getReceiptNo()));
        dto.setCreateTime(r.getCreateTime());
        dto.setUpdateTime(r.getUpdateTime());
        return dto;
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 1 -> "待确认";
            case 2 -> "已到账";
            case 3 -> "已核销";
            default -> "未知";
        };
    }

    private String getMethodDesc(Integer method) {
        return switch (method) {
            case 1 -> "银行转账";
            case 2 -> "扫码支付";
            case 3 -> "现金";
            default -> "未知";
        };
    }
}
