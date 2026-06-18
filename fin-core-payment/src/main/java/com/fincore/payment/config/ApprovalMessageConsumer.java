package com.fincore.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApprovalMessageConsumer {

    @RabbitListener(queues = RabbitMQConfig.APPROVAL_QUEUE)
    public void handleApprovalNotification(String message) {
        log.info("收到审批消息: {}", message);
        // TODO: 实际发送到小程序通知或邮件
    }
}
