package com.bookstore.audit.aspect;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Aspect responsible for auditing methods annotated with {@link Auditable}.
 *
 * <p>
 * The aspect records important information about business operations,
 * including the action, entity, authenticated user, HTTP request,
 * execution status, execution time, and exception information.
 * </p>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Intercepts methods annotated with {@link Auditable}.
     *
     * <p>
     * Security and HTTP request information are captured before the
     * business method executes.
     * </p>
     *
     * @param joinPoint intercepted method information
     * @return result returned by the intercepted method
     * @throws Throwable when the intercepted method throws an exception
     */
    @Around("@annotation(com.bookstore.audit.annotation.Auditable)")
    public Object audit(
            ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature =
                (MethodSignature) joinPoint.getSignature();

        Method method =
                methodSignature.getMethod();

        /*
         * When Spring AOP uses a proxy, the method obtained from the
         * proxy interface may not directly contain the annotation.
         *
         * Therefore, resolve the actual target method.
         */
        Method targetMethod =
                AopUtils.getMostSpecificMethod(
                        method,
                        joinPoint.getTarget().getClass()
                );

        Auditable auditable =
                targetMethod.getAnnotation(Auditable.class);

        /*
         * Defensive fallback.
         *
         * Normally the @Around pointcut guarantees that the annotation
         * exists, but this prevents a NullPointerException if the
         * method resolution behaves differently for a proxy.
         */
        if (auditable == null) {
            auditable =
                    method.getAnnotation(Auditable.class);
        }

        String action =
                auditable.action();

        String entity =
                auditable.entity();

        String methodName =
                targetMethod.getName();

        /*
         * -------------------------------------------------------------
         * CAPTURE SECURITY CONTEXT BEFORE BUSINESS METHOD EXECUTION
         * -------------------------------------------------------------
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                extractUsername(authentication);

        /*
         * -------------------------------------------------------------
         * CAPTURE HTTP REQUEST BEFORE BUSINESS METHOD EXECUTION
         * -------------------------------------------------------------
         */
        ServletRequestAttributes requestAttributes =
                getRequestAttributes();

        long startTime =
                System.currentTimeMillis();

        log.info(
                "AUDIT START | action={} | entity={} | method={} | username={}",
                action,
                entity,
                methodName,
                username
        );

        try {

            /*
             * Execute the actual business method.
             */
            Object result =
                    joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis() - startTime;

            AuditLog auditLog =
                    buildAuditLog(
                            action,
                            entity,
                            username,
                            requestAttributes,
                            executionTime,
                            true,
                            null
                    );

            auditService.save(auditLog);

            log.info(
                    "AUDIT SUCCESS | action={} | entity={} | method={} | executionTime={}ms",
                    action,
                    entity,
                    methodName,
                    executionTime
            );

            return result;

        } catch (Throwable exception) {

            long executionTime =
                    System.currentTimeMillis() - startTime;

            AuditLog auditLog =
                    buildAuditLog(
                            action,
                            entity,
                            username,
                            requestAttributes,
                            executionTime,
                            false,
                            exception
                    );

            auditService.save(auditLog);

            log.error(
                    "AUDIT FAILURE | action={} | entity={} | method={} | executionTime={}ms | exception={}",
                    action,
                    entity,
                    methodName,
                    executionTime,
                    exception.getClass().getSimpleName()
            );

            /*
             * Very important:
             *
             * Do not swallow the original exception.
             * The service/controller must still receive it.
             */
            throw exception;
        }
    }

    /**
     * Extracts the username from the supplied Authentication object.
     *
     * <p>
     * Authentication is supplied as an argument instead of reading
     * SecurityContextHolder again later. This guarantees that the
     * identity captured at the beginning of the operation is the one
     * written to the audit record.
     *
     * @param authentication current authentication
     * @return authenticated username, or null
     */
    private String extractUsername(
            Authentication authentication) {

        if (authentication == null) {
            return null;
        }

        if (!authentication.isAuthenticated()) {
            return null;
        }

        String username =
                authentication.getName();

        if (username == null || username.isBlank()) {
            return null;
        }

        return username;
    }

    /**
     * Retrieves the current HTTP request attributes.
     *
     * @return current servlet request attributes,
     *         or null when execution is outside an HTTP request
     */
    private ServletRequestAttributes getRequestAttributes() {

        return (ServletRequestAttributes)
                RequestContextHolder
                        .getRequestAttributes();
    }

    /**
     * Builds a persistent audit record.
     *
     * @param action business action
     * @param entity affected entity
     * @param username authenticated username
     * @param requestAttributes current HTTP request attributes
     * @param executionTime execution time in milliseconds
     * @param success whether operation succeeded
     * @param exception exception thrown by operation, if any
     * @return populated audit log
     */
    private AuditLog buildAuditLog(
            String action,
            String entity,
            String username,
            ServletRequestAttributes requestAttributes,
            long executionTime,
            boolean success,
            Throwable exception) {

        AuditLog auditLog =
                new AuditLog();

        auditLog.setAction(action);

        auditLog.setEntity(entity);

        auditLog.setUsername(username);

        auditLog.setExecutionTimeMs(executionTime);

        auditLog.setSuccess(success);

        captureHttpRequest(
                auditLog,
                requestAttributes
        );

        if (exception != null) {

            auditLog.setExceptionType(
                    exception.getClass().getSimpleName()
            );

            auditLog.setErrorMessage(
                    exception.getMessage()
            );
        }

        return auditLog;
    }

    /**
     * Captures information about the current HTTP request.
     *
     * @param auditLog audit record to populate
     * @param requestAttributes current request attributes
     */
    private void captureHttpRequest(
            AuditLog auditLog,
            ServletRequestAttributes requestAttributes) {

        if (requestAttributes == null) {
            return;
        }

        HttpServletRequest request =
                requestAttributes.getRequest();

        auditLog.setHttpMethod(
                request.getMethod()
        );

        auditLog.setRequestUri(
                request.getRequestURI()
        );

        auditLog.setIpAddress(
                request.getRemoteAddr()
        );
    }
}