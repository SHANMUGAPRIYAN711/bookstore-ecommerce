package com.bookstore.storage.service;

import com.bookstore.exception.BadRequestException;
import com.bookstore.storage.dto.FileUploadResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private MultipartFile multipartFile;

    private S3StorageService storageService;

    // ============================================================
    // SETUP
    // ============================================================

    @BeforeEach
    void setUp() {

        storageService =
                new S3StorageService(
                        s3Client,
                        s3Presigner
                );

        ReflectionTestUtils.setField(
                storageService,
                "bucketName",
                "test-bucket"
        );
    }

    // ============================================================
    // SUCCESSFUL UPLOAD
    // ============================================================

    @Test
    void uploadFile_shouldUploadSuccessfully()
            throws Exception {

        byte[] fileContent =
                "test-image-content".getBytes();

        // --------------------------------------------------------
        // Configure MultipartFile mock
        // --------------------------------------------------------

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

        // --------------------------------------------------------
        // Execute upload
        // --------------------------------------------------------

        FileUploadResponse response =
                storageService.uploadFile(
                        multipartFile
                );

        // --------------------------------------------------------
        // Verify response
        // --------------------------------------------------------

        assertNotNull(response);

        assertEquals(
                "cover.png",
                response.getFileName()
        );

        assertEquals(
                "image/png",
                response.getContentType()
        );

        assertEquals(
                fileContent.length,
                response.getSize()
        );

        assertNotNull(
                response.getObjectKey()
        );

        /*
         * Object key should be generated under
         * the uploads/ prefix.
         */
        assertTrue(
                response.getObjectKey()
                        .startsWith("uploads/")
        );

        /*
         * The original file extension should be preserved.
         */
        assertTrue(
                response.getObjectKey()
                        .endsWith(".png")
        );

        /*
         * Bucket is private.
         *
         * Therefore uploadFile() does not return
         * a permanent public S3 URL.
         */
        assertNull(
                response.getFileUrl()
        );

        // --------------------------------------------------------
        // Verify S3 upload
        // --------------------------------------------------------

        verify(s3Client)
                .putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                );
    }

    // ============================================================
    // NULL FILE
    // ============================================================

    @Test
    void uploadFile_shouldRejectNullFile() {

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                null
                        )
        );

        /*
         * S3 should never be contacted when validation
         * fails before upload.
         */
        verifyNoInteractions(
                s3Client
        );
    }

    // ============================================================
    // EMPTY FILE
    // ============================================================

    @Test
    void uploadFile_shouldRejectEmptyFile() {

        when(multipartFile.isEmpty())
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                multipartFile
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }

    // ============================================================
    // FILE ABOVE 10 MB
    // ============================================================

    @Test
    void uploadFile_shouldRejectFileAbove10Mb() {

        when(multipartFile.isEmpty())
                .thenReturn(false);

        when(multipartFile.getSize())
                .thenReturn(
                        10L * 1024 * 1024 + 1
                );

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                multipartFile
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }

    // ============================================================
    // UNSUPPORTED CONTENT TYPE
    // ============================================================

    @Test
    void uploadFile_shouldRejectUnsupportedContentType() {

        when(multipartFile.isEmpty())
                .thenReturn(false);

        when(multipartFile.getSize())
                .thenReturn(1024L);

        when(multipartFile.getContentType())
                .thenReturn("application/zip");

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                multipartFile
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }

    // ============================================================
    // MISSING CONTENT TYPE
    // ============================================================

    @Test
    void uploadFile_shouldRejectMissingContentType() {

        when(multipartFile.isEmpty())
                .thenReturn(false);

        when(multipartFile.getSize())
                .thenReturn(1024L);

        when(multipartFile.getContentType())
                .thenReturn(null);

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                multipartFile
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }

    // ============================================================
    // MISSING FILE NAME
    // ============================================================

    @Test
    void uploadFile_shouldRejectMissingFileName() {

        when(multipartFile.isEmpty())
                .thenReturn(false);

        when(multipartFile.getSize())
                .thenReturn(1024L);

        when(multipartFile.getContentType())
                .thenReturn("image/png");

        when(multipartFile.getOriginalFilename())
                .thenReturn(null);

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                multipartFile
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }

    // ============================================================
    // BLANK FILE NAME
    // ============================================================

    @Test
    void uploadFile_shouldRejectBlankFileName() {

        when(multipartFile.isEmpty())
                .thenReturn(false);

        when(multipartFile.getSize())
                .thenReturn(1024L);

        when(multipartFile.getContentType())
                .thenReturn("image/png");

        when(multipartFile.getOriginalFilename())
                .thenReturn("   ");

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                multipartFile
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }
}