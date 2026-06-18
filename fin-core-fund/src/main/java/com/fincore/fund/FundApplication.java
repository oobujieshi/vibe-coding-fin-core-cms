package com.fincore.fund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.fincore.common", "com.fincore.fund"})
@MapperScan({"com.fincore.fund.mapper", "com.fincore.common.base.mapper"})
public class FundApplication {
    public static void main(String[] args) {
        SpringApplication.run(FundApplication.class, args);
    }
}
