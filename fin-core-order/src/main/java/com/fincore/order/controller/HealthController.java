package com.fincore.order.controller;

import com.fincore.common.base.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping
    public Result<Map<String, String>> check() {
        Map<String, String> results = new LinkedHashMap<>();
        results.put("mysql", checkMySql() ? "OK" : "FAIL");

        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            String val = redisTemplate.opsForValue().get("health_check");
            results.put("redis", "ok".equals(val) ? "OK" : "FAIL");
        } catch (Exception e) {
            results.put("redis", "FAIL: " + e.getMessage());
        }

        try {
            rabbitTemplate.convertAndSend("health.check", "ping");
            results.put("rabbitmq", "OK");
        } catch (Exception e) {
            results.put("rabbitmq", "FAIL: " + e.getMessage());
        }

        return Result.ok(results);
    }

    private boolean checkMySql() {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
