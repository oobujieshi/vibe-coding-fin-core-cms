package com.fincore.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.fincore.common", "com.fincore.report"})
@MapperScan({"com.fincore.order.mapper", "com.fincore.payment.mapper", "com.fincore.fund.mapper", "com.fincore.common.base.mapper"})
public class ReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
