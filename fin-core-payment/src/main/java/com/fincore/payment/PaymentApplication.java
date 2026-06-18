package com.fincore.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.fincore.common", "com.fincore.payment"})
@MapperScan({"com.fincore.payment.mapper", "com.fincore.common.base.mapper"})
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
