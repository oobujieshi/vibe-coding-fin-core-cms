package com.fincore.common.web.aspect;

import com.fincore.common.base.annotation.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String operation = auditLog.operation();
        String targetType = auditLog.targetType();
        String method = ((MethodSignature) point.getSignature()).getMethod().getName();
        String args = Arrays.toString(point.getArgs());

        try {
            Object result = point.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("[AUDIT] op={} target={} method={} args={} cost={}ms result=OK",
                    operation, targetType, method, args, cost);
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[AUDIT] op={} target={} method={} args={} cost={}ms result=FAIL error={}",
                    operation, targetType, method, args, cost, e.getMessage());
            throw e;
        }
    }
}
