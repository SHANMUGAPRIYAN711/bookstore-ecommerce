package com.bookstore.storage.service;

import com.bookstore.exception.BadRequestException;
import com.bookstore.storage.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * AWS S3 implementation of the storage service.
 *
 * <p>
 * Responsible for validating uploaded files, generating unique
 * object keys, storing objects in S3, and returning their URLs.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    /**
     * Maximum allowed file size.
     *
     * <p>
     * The application configuration also limits multipart uploads
     * to 10 MB.
     * </p>
     */
    private static final long MAX_FILE_SIZE =
            10 * 1024 * 1024L;

    /**
     * Supported file types.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "application/pdf"
            );

    /**
     * Shared AWS S3 client.
     */
    private final S3Client s3Client;

    /**
     * S3 bucket configured through application properties.
     */
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Uploads a file to Amazon S3.
     */
    @Override
    public FileUploadResponse uploadFile(
            MultipartFile file
    ) {

        validateFile(file);

        String originalFileName =
                file.getOriginalFilename();

        String objectKey =
                generateObjectKey(
                        originalFileName
                );

        try {

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(
                                    file.getContentType()
                            )
                            .contentLength(
                                    file.getSize()
                            )
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            String fileUrl =
                    s3Client.utilities()
                            .getUrl(
                                    GetUrlRequest.builder()
                                            .bucket(bucketName)
                                            .key(objectKey)
                                            .build()
                            )
                            .toExternalForm();

            return FileUploadResponse.builder()
                    .fileName(originalFileName)
                    .objectKey(objectKey)
                    .fileUrl(fileUrl)
                    .contentType(
                            file.getContentType()
                    )
                    .size(file.getSize())
                    .build();

        } catch (IOException exception) {

            throw new BadRequestException(
                    "Failed to read uploaded file"
            );

        } catch (S3Exception exception) {

            throw new IllegalStateException(
                    "Failed to upload file to S3",
                    exception
            );
        }
    }

    /**
     * Validates the uploaded file before storage.
     */
    private void validateFile(
            MultipartFile file
    ) {

        if (file == null ||
                file.isEmpty()) {

            throw new BadRequestException(
                    "File is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new BadRequestException(
                    "File size must not exceed 10 MB"
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase()
                )) {

            throw new BadRequestException(
                    "Unsupported file type"
            );
        }

        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.isBlank()) {

            throw new BadRequestException(
                    "File name is required"
            );
        }
    }

    /**
     * Generates a unique S3 object key while retaining
     * the original file extension.
     */
    private String generateObjectKey(
            String originalFileName
    ) {

        String extension = "";

        int lastDot =
                originalFileName.lastIndexOf('.');

        if (lastDot >= 0 &&
                lastDot < originalFileName.length() - 1) {

            extension =
                    originalFileName.substring(
                            lastDot
                    ).toLowerCase();
        }

        return UUID.randomUUID()
                + extension;
    }
}