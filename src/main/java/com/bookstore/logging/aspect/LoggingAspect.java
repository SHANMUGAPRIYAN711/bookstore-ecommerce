package com.bookstore.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect responsible for logging application method execution.
 *
 * <p>
 * This aspect provides centralized logging for controller and service
 * methods without requiring logging code to be repeated inside each
 * business method.
 * </p>
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Logs method entry, successful completion, execution time,
     * and failures for controller and service methods.
     *
     * @param joinPoint provides information about the intercepted method
     * @return result returned by the intercepted method
     * @throws Throwable when the intercepted method throws an exception
     */
    @Around(
            "execution(* com.bookstore..controller..*(..)) " +
                    "|| execution(* com.bookstore..service..*(..))"
    )
    public Object logExecution(
            ProceedingJoinPoint joinPoint) throws Throwable {

        String className =
                joinPoint.getSignature()
                        .getDeclaringTypeName();

        String methodName =
                joinPoint.getSignature()
                        .getName();

        long startTime =
                System.currentTimeMillis();

        log.info(
                "METHOD START | class={} | method={}",
                className,
                methodName
        );

        try {

            Object result = joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis() - startTime;

            log.info(
                    "METHOD SUCCESS | class={} | method={} | executionTime={}ms",
                    className,
                    methodName,
                    executionTime
            );

            return result;

        } catch (Throwable exception) {

            long executionTime =
                    System.currentTimeMillis() - startTime;

            log.error(
                    "METHOD FAILURE | class={} | method={} | executionTime={}ms | exception={}",
                    className,
                    methodName,
                    executionTime,
                    exception.getClass().getSimpleName()
            );

            throw exception;
        }
    }
}