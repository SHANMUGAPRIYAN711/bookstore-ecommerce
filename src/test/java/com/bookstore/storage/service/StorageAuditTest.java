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
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;
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
    private S3Utilities s3Utilities;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private AuditService auditService;

    private StorageService storageService;

    @BeforeEach
    void setUp() {

        S3StorageService target =
                new S3StorageService(
                        s3Client
                );

        ReflectionTestUtils.setField(
                target,
                "bucketName",
                "test-bucket"
        );

        AuditAspect auditAspect =
                new AuditAspect(
                        auditService
                );

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

    @AfterEach
    void cleanup() {

        SecurityContextHolder.clearContext();

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void uploadFile_shouldCreateSuccessfulAuditLog()
            throws Exception {

        byte[] fileContent =
                "test-image-content".getBytes();

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

        when(s3Client.utilities())
                .thenReturn(
                        s3Utilities
                );

        when(
                s3Utilities.getUrl(
                        any(GetUrlRequest.class)
                )
        ).thenReturn(
                URI.create(
                        "https://test-bucket.s3.ap-south-1.amazonaws.com/test-key.png"
                ).toURL()
        );

        configureRequest(
                "POST",
                "/api/storage/upload"
        );

        var response =
                storageService.uploadFile(
                        multipartFile
                );

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

        assertTrue(
                response.getObjectKey()
                        .endsWith(".png")
        );

        assertEquals(
                "https://test-bucket.s3.ap-south-1.amazonaws.com/test-key.png",
                response.getFileUrl()
        );

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