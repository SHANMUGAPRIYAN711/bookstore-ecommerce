package com.bookstore.audit.aspect;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

class AuditAspectTest {

    @Test
    void auditableMethod_shouldExecuteSuccessfully() {

        AuditService auditService =
                mock(AuditService.class);

        TestService target =
                new TestService();

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        TestService proxy =
                factory.getProxy();

        String result =
                proxy.createBook();

        assertEquals(
                "Book created",
                result
        );

        ArgumentCaptor<AuditLog> auditLogCaptor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(auditLogCaptor.capture());

        AuditLog auditLog =
                auditLogCaptor.getValue();

        assertEquals(
                "CREATE_BOOK",
                auditLog.getAction()
        );

        assertEquals(
                "BOOK",
                auditLog.getEntity()
        );

        assertEquals(
                true,
                auditLog.isSuccess()
        );

        assertEquals(
                null,
                auditLog.getExceptionType()
        );

        assertEquals(
                null,
                auditLog.getErrorMessage()
        );
    }

    @Test
    void auditableMethod_shouldLogFailureAndRethrowException() {

        AuditService auditService =
                mock(AuditService.class);

        TestService target =
                new TestService();

        AuditAspect auditAspect =
                new AuditAspect(auditService);

        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        TestService proxy =
                factory.getProxy();

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        proxy::failOperation
                );

        assertEquals(
                "Book creation failed",
                exception.getMessage()
        );

        ArgumentCaptor<AuditLog> auditLogCaptor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditService)
                .save(auditLogCaptor.capture());

        AuditLog auditLog =
                auditLogCaptor.getValue();

        assertEquals(
                "CREATE_BOOK",
                auditLog.getAction()
        );

        assertEquals(
                "BOOK",
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
                "Book creation failed",
                auditLog.getErrorMessage()
        );
    }

    static class TestService {

        @Auditable(
                action = "CREATE_BOOK",
                entity = "BOOK"
        )
        public String createBook() {

            return "Book created";
        }

        @Auditable(
                action = "CREATE_BOOK",
                entity = "BOOK"
        )
        public String failOperation() {

            throw new RuntimeException(
                    "Book creation failed"
            );
        }
    }
}