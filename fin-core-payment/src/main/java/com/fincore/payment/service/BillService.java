package com.fincore.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.exception.BusinessException;
import com.fincore.payment.dto.BillDTO;
import com.fincore.payment.dto.BillMergeRequest;
import com.fincore.payment.dto.BillSplitRequest;
import com.fincore.payment.entity.Bill;
import com.fincore.payment.mapper.BillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillMapper billMapper;

    public Page<BillDTO> page(int page, int size, Long customerId, Integer sendStatus,
                               String startDate, String endDate) {
        LambdaQueryWrapper<Bill> qw = new LambdaQueryWrapper<>();
        if (customerId != null) qw.eq(Bill::getCustomerId, customerId);
        if (sendStatus != null) qw.eq(Bill::getSendStatus, sendStatus);
        if (startDate != null) qw.ge(Bill::getCreateTime, startDate);
        if (endDate != null) qw.le(Bill::getCreateTime, endDate + " 23:59:59");
        qw.orderByDesc(Bill::getCreateTime);

        Page<Bill> pr = billMapper.selectPage(new Page<>(page, size), qw);
        Page<BillDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return result;
    }

    public BillDTO getById(Long id) {
        Bill b = billMapper.selectById(id);
        if (b == null) throw new BusinessException(404, "账单不存在");
        return toDTO(b);
    }

    @Transactional
    public BillDTO create(BillDTO dto) {
        Bill b = new Bill();
        b.setBillNo("BILL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        b.setCustomerId(dto.getCustomerId());
        b.setBillPeriodStart(dto.getBillPeriodStart());
        b.setBillPeriodEnd(dto.getBillPeriodEnd());
        b.setTotalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO);
        b.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO);
        b.setSendStatus(0);
        billMapper.insert(b);
        log.info("账单已生成: id={}, billNo={}, customerId={}, amount={}", b.getId(), b.getBillNo(), b.getCustomerId(), b.getTotalAmount());
        return toDTO(b);
    }

    @Transactional
    public BillDTO merge(BillMergeRequest req) {
        if (req.getBillIds() == null || req.getBillIds().size() < 2) {
            throw new BusinessException(400, "至少选择2个账单进行合并");
        }

        List<Bill> bills = billMapper.selectBatchIds(req.getBillIds());
        if (bills.size() != req.getBillIds().size()) {
            throw new BusinessException(404, "部分账单不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        LocalDate earliestStart = null;
        LocalDate latestEnd = null;

        for (Bill b : bills) {
            totalAmount = totalAmount.add(b.getTotalAmount());
            paidAmount = paidAmount.add(b.getPaidAmount());
            if (earliestStart == null || b.getBillPeriodStart().isBefore(earliestStart)) {
                earliestStart = b.getBillPeriodStart();
            }
            if (latestEnd == null || b.getBillPeriodEnd().isAfter(latestEnd)) {
                latestEnd = b.getBillPeriodEnd();
            }
            // 软删除原账单
            b.setSendStatus(-1); // -1=已合并
            billMapper.updateById(b);
        }

        Bill merged = new Bill();
        merged.setBillNo("BILL-M-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        merged.setCustomerId(bills.get(0).getCustomerId());
        merged.setBillPeriodStart(earliestStart);
        merged.setBillPeriodEnd(latestEnd);
        merged.setTotalAmount(totalAmount);
        merged.setPaidAmount(paidAmount);
        merged.setSendStatus(0);
        billMapper.insert(merged);
        log.info("账单已合并: id={}, billNo={}, fromBills={}, amount={}", merged.getId(), merged.getBillNo(), req.getBillIds(), totalAmount);

        return toDTO(merged);
    }

    @Transactional
    public List<BillDTO> split(BillSplitRequest req) {
        Bill source = billMapper.selectById(req.getBillId());
        if (source == null) throw new BusinessException(404, "账单不存在");
        if (source.getSendStatus() != 0) throw new BusinessException(409, "仅未发送的账单可拆分");

        BigDecimal sum = req.getSplitAmounts().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(source.getTotalAmount()) != 0) {
            throw new BusinessException(400, "拆分金额合计(" + sum + ")与账单总额(" + source.getTotalAmount() + ")不一致");
        }

        // 软删除原账单
        source.setSendStatus(-1); // -1=已拆分
        billMapper.updateById(source);

        List<BillDTO> result = new ArrayList<>();
        for (int i = 0; i < req.getSplitAmounts().size(); i++) {
            Bill split = new Bill();
            split.setBillNo("BILL-S-" + source.getId() + "-" + (i + 1) + "-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
            split.setCustomerId(source.getCustomerId());
            split.setBillPeriodStart(source.getBillPeriodStart());
            split.setBillPeriodEnd(source.getBillPeriodEnd());
            split.setTotalAmount(req.getSplitAmounts().get(i));
            split.setPaidAmount(BigDecimal.ZERO);
            split.setSendStatus(0);
            billMapper.insert(split);
            result.add(toDTO(split));
        }
        log.info("账单已拆分: sourceId={}, 拆成{}笔", source.getId(), req.getSplitAmounts().size());

        return result;
    }

    @Transactional
    public void send(Long id) {
        Bill b = billMapper.selectById(id);
        if (b == null) throw new BusinessException(404, "账单不存在");
        if (b.getSendStatus() == 1) throw new BusinessException(409, "账单已发送");
        if (b.getSendStatus() == -1) throw new BusinessException(409, "账单已合并/拆分，不可发送");
        b.setSendStatus(1);
        billMapper.updateById(b);
        log.info("账单{}已发送给客户{}", b.getBillNo(), b.getCustomerId());
    }

    private BillDTO toDTO(Bill b) {
        BillDTO dto = new BillDTO();
        dto.setId(b.getId());
        dto.setBillNo(b.getBillNo());
        dto.setCustomerId(b.getCustomerId());
        dto.setBillPeriodStart(b.getBillPeriodStart());
        dto.setBillPeriodEnd(b.getBillPeriodEnd());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setPaidAmount(b.getPaidAmount());
        dto.setUnpaidAmount(b.getTotalAmount().subtract(b.getPaidAmount()));
        dto.setSendStatus(b.getSendStatus());
        dto.setSendStatusDesc(switch (b.getSendStatus()) {
            case 0 -> "未发送";
            case 1 -> "已发送";
            case -1 -> "已合并/已拆分";
            default -> "未知";
        });
        dto.setCreateTime(b.getCreateTime());
        dto.setUpdateTime(b.getUpdateTime());
        return dto;
    }
}
