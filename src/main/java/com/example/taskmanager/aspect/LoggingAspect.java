package com.example.taskmanager.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.example.taskmanager.annotation.Loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long started = System.currentTimeMillis();
        log.info("Start: {}", joinPoint.getSignature().toShortString());
        try {
            return joinPoint.proceed();
        } finally {
            log.info("Finished: {} ({} ms)", joinPoint.getSignature().toShortString(),
                    System.currentTimeMillis() - started);
        }
    }
}
