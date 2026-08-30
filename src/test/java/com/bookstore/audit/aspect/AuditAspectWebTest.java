package com.bookstore.audit.aspect;

import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditAspectWebTest {

    @Test
    void audit_shouldCaptureHttpAndUserInformation() {

        // Mock AuditService because this is an AOP unit test.
        AuditService auditService =
                mock(AuditService.class);

        // Create the aspect with the mocked AuditService.
        AuditAspect auditAspect =
                new AuditAspect(auditService);

        // Create the actual target object.
        TestService target =
                new TestService();

        // Create an AOP proxy around the target.
        AspectJProxyFactory factory =
                new AspectJProxyFactory(target);

        factory.addAspect(auditAspect);

        TestService proxy =
                factory.getProxy();

        // ---------------------------------------------------------
        // Create mock HTTP request
        // ---------------------------------------------------------

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("POST");

        request.setRequestURI(
                "/api/books"
        );

        request.setRemoteAddr(
                "192.168.1.10"
        );

        // Make the request available through RequestContextHolder.
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        // ---------------------------------------------------------
        // Create authenticated Spring Security user
        // ---------------------------------------------------------

        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "admin@bookstore.com",
                                null,
                                Collections.emptyList()
                        )
                );

        try {

            // -----------------------------------------------------
            // Execute the proxied method.
            // This should trigger AuditAspect.
            // -----------------------------------------------------

            String result =
                    proxy.createBook();

            assertEquals(
                    "Book created",
                    result
            );

            // -----------------------------------------------------
            // Capture the AuditLog passed to AuditService.save()
            // -----------------------------------------------------

            ArgumentCaptor<AuditLog> captor =
                    ArgumentCaptor.forClass(AuditLog.class);

            verify(auditService)
                    .save(captor.capture());

            AuditLog auditLog =
                    captor.getValue();

            // -----------------------------------------------------
            // Verify audit information
            // -----------------------------------------------------

            assertEquals(
                    "CREATE_BOOK",
                    auditLog.getAction()
            );

            assertEquals(
                    "BOOK",
                    auditLog.getEntity()
            );

            assertEquals(
                    "admin@bookstore.com",
                    auditLog.getUsername()
            );

            assertEquals(
                    "POST",
                    auditLog.getHttpMethod()
            );

            assertEquals(
                    "/api/books",
                    auditLog.getRequestUri()
            );

            assertEquals(
                    "192.168.1.10",
                    auditLog.getIpAddress()
            );

            assertEquals(
                    true,
                    auditLog.isSuccess()
            );

        } finally {

            // Always clean the thread-local security context.
            SecurityContextHolder.clearContext();

            // Always clean the thread-local request context.
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     * Simple target service used to verify
     * that the AuditAspect intercepts annotated methods.
     */
    static class TestService {

        @com.bookstore.audit.annotation.Auditable(
                action = "CREATE_BOOK",
                entity = "BOOK"
        )
        public String createBook() {

            return "Book created";
        }
    }
}