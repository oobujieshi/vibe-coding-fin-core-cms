package com.fincore.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.fincore.common", "com.fincore.order"})
@EnableDiscoveryClient
@MapperScan({"com.fincore.order.mapper", "com.fincore.common.base.mapper"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
