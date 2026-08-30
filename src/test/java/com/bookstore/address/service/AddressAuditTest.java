package com.bookstore.address.service;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressAuditTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Test
    void audit_shouldSaveSuccessfulAuditLog() throws Throwable {

        AuditTestTarget target = new AuditTestTarget();

        Method method =
                AuditTestTarget.class.getMethod("createAddress");

        when(joinPoint.getSignature())
                .thenReturn(methodSignature);

        when(methodSignature.getMethod())
                .thenReturn(method);

        when(joinPoint.getTarget())
                .thenReturn(target);

        when(joinPoint.proceed())
                .thenReturn("success");

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        Object result =
                auditAspect.audit(joinPoint);

        assertNotNull(result);

        assertEquals(
                "success",
                result
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        AuditLog auditLog =
                captor.getValue();

        assertEquals(
                "CREATE_ADDRESS",
                auditLog.getAction()
        );

        assertEquals(
                "ADDRESS",
                auditLog.getEntity()
        );

        assertEquals(
                true,
                auditLog.isSuccess()
        );

        assertNotNull(
                auditLog.getExecutionTimeMs()
        );
    }

    @Test
    void audit_shouldSaveFailedAuditLog() throws Throwable {

        AuditTestTarget target =
                new AuditTestTarget();

        Method method =
                AuditTestTarget.class.getMethod("createAddress");

        RuntimeException exception =
                new RuntimeException("Something went wrong");

        when(joinPoint.getSignature())
                .thenReturn(methodSignature);

        when(methodSignature.getMethod())
                .thenReturn(method);

        when(joinPoint.getTarget())
                .thenReturn(target);

        when(joinPoint.proceed())
                .thenThrow(exception);

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        RuntimeException thrown =
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> auditAspect.audit(joinPoint)
                );

        assertEquals(
                "Something went wrong",
                thrown.getMessage()
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(captor.capture());

        AuditLog auditLog =
                captor.getValue();

        assertEquals(
                "CREATE_ADDRESS",
                auditLog.getAction()
        );

        assertEquals(
                "ADDRESS",
                auditLog.getEntity()
        );

        assertEquals(
                false,
                auditLog.isSuccess()
        );

        assertEquals(
                "RuntimeException",
                auditLog.getExceptionType()
        );

        assertEquals(
                "Something went wrong",
                auditLog.getErrorMessage()
        );
    }

    /**
     * Small test target containing the @Auditable annotation.
     */
    static class AuditTestTarget {

        @Auditable(
                action = "CREATE_ADDRESS",
                entity = "ADDRESS"
        )
        public String createAddress() {

            return "success";
        }
    }
}