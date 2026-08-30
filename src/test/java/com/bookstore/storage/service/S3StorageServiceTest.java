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
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private S3Utilities s3Utilities;

    @Mock
    private MultipartFile multipartFile;

    private S3StorageService storageService;

    @BeforeEach
    void setUp() {

        storageService =
                new S3StorageService(
                        s3Client
                );

        ReflectionTestUtils.setField(
                storageService,
                "bucketName",
                "test-bucket"
        );
    }

    @Test
    void uploadFile_shouldUploadSuccessfully()
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
                .thenReturn(s3Utilities);

        when(
                s3Utilities.getUrl(
                        any(GetUrlRequest.class)
                )
        ).thenReturn(
                URI.create(
                        "https://test-bucket.s3.ap-south-1.amazonaws.com/test-key.png"
                ).toURL()
        );

        FileUploadResponse response =
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

        assertEquals(
                fileContent.length,
                response.getSize()
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

        verify(s3Client)
                .putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                );

        verify(s3Client.utilities())
                .getUrl(
                        any(GetUrlRequest.class)
                );
    }

    @Test
    void uploadFile_shouldRejectNullFile() {

        assertThrows(
                BadRequestException.class,
                () ->
                        storageService.uploadFile(
                                null
                        )
        );

        verifyNoInteractions(
                s3Client
        );
    }

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