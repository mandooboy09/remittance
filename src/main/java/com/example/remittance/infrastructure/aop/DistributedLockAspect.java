package com.example.remittance.infrastructure.aop;

import com.example.remittance.application.annotation.DistributedLock;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    private static final String LOCK = "lock:";

    @Around("@annotation(distributedLock)")
    public Object aroundMethod(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        String[] keys = parseSpel(distributedLock.keys(), paramNames, args);

        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        List<RLock> locks = Arrays.stream(keys)
                .map(key -> redissonClient.getLock(LOCK + key))
                .toList();

        try {
            for (RLock lock : locks) {
                boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
                if (!isLocked) {
                    throw new IllegalStateException("not obtain lock: " + lock.getName());
                }
                log.info("success obtain lock : " + lock.getName());
            }

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        unlockLocks(locks);
                    }
                });
            }

            return joinPoint.proceed();
        } finally {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                unlockLocks(locks);
            }
        }
    }

    private void unlockLocks(List<RLock> locks) {
        for (RLock lock : locks) {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("unlock after execution: " + lock.getName());
            }
        }
    }

    private String[] parseSpel(String[] keys, String[] paramNames, Object[] args) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        StringBuilder keyBuilder = new StringBuilder();
        for (String key : keys) {
            try {
                Expression expression = parser.parseExpression(key);
                Object value = expression.getValue(context);
                keyBuilder.append(value).append(" ");
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse SpEL: " + key, e);
            }
        }

        return keyBuilder.substring(0, keyBuilder.length() - 1).split(" ");
    }
}
