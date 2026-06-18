package com.fincore.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fincore.common.base.exception.BusinessException;
import com.fincore.payment.dto.ApprovalDTO;
import com.fincore.payment.entity.ApprovalFlow;
import com.fincore.payment.mapper.ApprovalFlowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalFlowMapper approvalFlowMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void createApprovalFlow(String bizType, Long bizId) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setCurrentNode(1);
        flow.setTotalNodes(1); // 默认1级审批
        flow.setNodeConfig("[{\"node\":1,\"role\":\"FINANCE\"},{\"node\":2,\"role\":\"ADMIN\"}]");
        flow.setApprovalStatus(1); // 审批中
        approvalFlowMapper.insert(flow);

        // 推送到 RabbitMQ 通知审批人
        sendApprovalNotification(bizType, bizId, 1, "新的审批申请");
    }

    @Transactional
    public void processNode(ApprovalFlow flow, String action, String comment, Long approvedBy) {
        if (!"APPROVE".equals(action) && !"REJECT".equals(action) && !"TRANSFER".equals(action)) {
            throw new BusinessException(400, "无效的审批动作: " + action);
        }

        flow.setApprovedBy(approvedBy);
        flow.setApprovedTime(LocalDateTime.now());
        flow.setComment(comment);

        switch (action) {
            case "APPROVE":
                if (flow.getCurrentNode() >= flow.getTotalNodes()) {
                    flow.setApprovalStatus(2); // 通过
                    sendApprovalNotification(flow.getBizType(), flow.getBizId(), 2, "审批已通过");
                } else {
                    flow.setCurrentNode(flow.getCurrentNode() + 1);
                    flow.setApprovalStatus(1); // 仍审批中
                    sendApprovalNotification(flow.getBizType(), flow.getBizId(), flow.getCurrentNode(), "审批进入下一节点");
                }
                break;
            case "REJECT":
                flow.setApprovalStatus(3); // 驳回
                sendApprovalNotification(flow.getBizType(), flow.getBizId(), 3, "审批已驳回: " + comment);
                break;
            case "TRANSFER":
                // 转审：保持当前节点，变更审批人
                sendApprovalNotification(flow.getBizType(), flow.getBizId(), flow.getCurrentNode(), "审批已转审");
                break;
        }

        approvalFlowMapper.updateById(flow);
    }

    public Page<ApprovalDTO> getPendingApprovals(int page, int size, String bizType, Long approverId) {
        LambdaQueryWrapper<ApprovalFlow> qw = new LambdaQueryWrapper<>();
        qw.eq(ApprovalFlow::getApprovalStatus, 1); // 审批中
        if (bizType != null && !bizType.isBlank()) qw.eq(ApprovalFlow::getBizType, bizType);
        qw.orderByDesc(ApprovalFlow::getCreateTime);

        Page<ApprovalFlow> pr = approvalFlowMapper.selectPage(new Page<>(page, size), qw);
        Page<ApprovalDTO> result = new Page<>(page, size, pr.getTotal());
        result.setRecords(pr.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return result;
    }

    public ApprovalDTO getApprovalById(Long id) {
        ApprovalFlow f = approvalFlowMapper.selectById(id);
        if (f == null) throw new BusinessException(404, "审批记录不存在");
        return toDTO(f);
    }

    @Transactional
    public ApprovalDTO processApproval(Long id, String action, String comment, Long approvedBy) {
        ApprovalFlow flow = approvalFlowMapper.selectById(id);
        if (flow == null) throw new BusinessException(404, "审批记录不存在");
        if (flow.getApprovalStatus() != 1) throw new BusinessException(409, "该审批已处理");

        processNode(flow, action, comment, approvedBy);
        return toDTO(flow);
    }

    private void sendApprovalNotification(String bizType, Long bizId, int nodeOrStatus, String message) {
        try {
            rabbitTemplate.convertAndSend("fincore.approval.exchange", "approval.notify", message);
            log.info("审批消息已推送: bizType={}, bizId={}, msg={}", bizType, bizId, message);
        } catch (Exception e) {
            log.warn("审批消息推送失败(非致命): {}", e.getMessage());
        }
    }

    private ApprovalDTO toDTO(ApprovalFlow f) {
        ApprovalDTO dto = new ApprovalDTO();
        dto.setId(f.getId());
        dto.setBizType(f.getBizType());
        dto.setBizId(f.getBizId());
        dto.setCurrentNode(f.getCurrentNode());
        dto.setTotalNodes(f.getTotalNodes());
        dto.setNodeConfig(f.getNodeConfig());
        dto.setApprovalStatus(f.getApprovalStatus());
        dto.setApprovalStatusDesc(switch (f.getApprovalStatus()) {
            case 1 -> "审批中";
            case 2 -> "已通过";
            case 3 -> "已驳回";
            default -> "未知";
        });
        dto.setApprovedBy(f.getApprovedBy());
        dto.setApprovedTime(f.getApprovedTime());
        dto.setComment(f.getComment());
        dto.setCreateTime(f.getCreateTime());
        dto.setUpdateTime(f.getUpdateTime());
        return dto;
    }
}
