package com.bookstore.storage.service;

import com.bookstore.audit.aspect.AuditAspect;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.service.AuditService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageAuditTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private AuditService auditService;

    @Mock
    private S3Presigner s3Presigner;

    private StorageService storageService;

    // ============================================================
    // SETUP
    // ============================================================

    @BeforeEach
    void setUp() {

        /*
         * Create the real S3StorageService using mocked AWS clients.
         */
        S3StorageService target =
                new S3StorageService(
                        s3Client,
                        s3Presigner
                );

        /*
         * Inject test bucket name into the private field.
         */
        ReflectionTestUtils.setField(
                target,
                "bucketName",
                "test-bucket"
        );

        /*
         * Create the real AuditAspect with mocked AuditService.
         */
        AuditAspect auditAspect =
                new AuditAspect(
                        auditService
                );

        /*
         * Create an AOP proxy so that @Auditable methods
         * are intercepted by AuditAspect.
         */
        AspectJProxyFactory factory =
                new AspectJProxyFactory(
                        target
                );

        factory.addAspect(
                auditAspect
        );

        storageService =
                factory.getProxy();

        configureSecurity();
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();

        RequestContextHolder.resetRequestAttributes();
    }

    // ============================================================
    // UPLOAD FILE + AUDIT
    // ============================================================

    @Test
    void uploadFile_shouldCreateSuccessfulAuditLog()
            throws Exception {

        byte[] fileContent =
                "test-image-content".getBytes();

        /*
         * Configure MultipartFile mock.
         */
        when(multipartFile.isEmpty())
                .thenReturn(false);

        when(multipartFile.getSize())
                .thenReturn(
                        (long) fileContent.length
                );

        when(multipartFile.getContentType())
                .thenReturn("image/png");

        when(multipartFile.getOriginalFilename())
                .thenReturn("cover.png");

        when(multipartFile.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                fileContent
                        )
                );

        /*
         * We intentionally DO NOT mock:
         *
         * s3Client.utilities()
         * S3Utilities
         * GetUrlRequest
         *
         * because the current implementation uses a private
         * S3 bucket and does not generate a permanent public URL
         * during upload.
         */

        configureRequest(
                "POST",
                "/api/storage/upload"
        );

        /*
         * Execute the real StorageService through the
         * AuditAspect proxy.
         */
        var response =
                storageService.uploadFile(
                        multipartFile
                );

        // ========================================================
        // VERIFY STORAGE RESPONSE
        // ========================================================

        assertNotNull(response);

        assertEquals(
                "cover.png",
                response.getFileName()
        );

        assertEquals(
                "image/png",
                response.getContentType()
        );

        assertNotNull(
                response.getObjectKey()
        );

        /*
         * The S3 object key should start with uploads/
         * and end with the original file extension.
         */
        assertTrue(
                response.getObjectKey()
                        .startsWith("uploads/")
        );

        assertTrue(
                response.getObjectKey()
                        .endsWith(".png")
        );

        /*
         * The bucket is private.
         *
         * Therefore uploadFile() does not return a public
         * permanent URL.
         */
        assertEquals(
                null,
                response.getFileUrl()
        );

        // ========================================================
        // VERIFY AUDIT LOG
        // ========================================================

        ArgumentCaptor<AuditLog> auditLogCaptor =
                ArgumentCaptor.forClass(
                        AuditLog.class
                );

        verify(auditService)
                .save(
                        auditLogCaptor.capture()
                );

        AuditLog auditLog =
                auditLogCaptor.getValue();

        assertNotNull(auditLog);

        assertEquals(
                "UPLOAD_FILE",
                auditLog.getAction()
        );

        assertEquals(
                "STORAGE",
                auditLog.getEntity()
        );

        assertEquals(
                "sauvik@bookstore.com",
                auditLog.getUsername()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/storage/upload",
                auditLog.getRequestUri()
        );

        assertEquals(
                "192.168.1.10",
                auditLog.getIpAddress()
        );

        assertTrue(
                auditLog.isSuccess()
        );
    }

    // ============================================================
    // SECURITY CONFIGURATION
    // ============================================================

    private void configureSecurity() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "sauvik@bookstore.com",
                        null,
                        Collections.emptyList()
                );

        SecurityContext securityContext =
                SecurityContextHolder
                        .createEmptyContext();

        securityContext.setAuthentication(
                authentication
        );

        SecurityContextHolder.setContext(
                securityContext
        );
    }

    // ============================================================
    // HTTP REQUEST CONFIGURATION
    // ============================================================

    private void configureRequest(
            String method,
            String uri
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod(method);

        request.setRequestURI(uri);

        request.setRemoteAddr(
                "192.168.1.10"
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(
                        request
                )
        );
    }
}