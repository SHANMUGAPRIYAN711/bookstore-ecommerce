package com.bookstore.storage.service;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.exception.BadRequestException;
import com.bookstore.storage.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * AWS S3 implementation of the storage service.
 *
 * <p>
 * Responsible for validating uploaded files, generating unique
 * S3 object keys, uploading files to a private S3 bucket, and
 * generating temporary presigned URLs for accessing stored objects.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    /**
     * Maximum allowed file size: 10 MB.
     */
    private static final long MAX_FILE_SIZE =
            10 * 1024 * 1024L;

    /**
     * S3 prefix used for uploaded files.
     */
    private static final String UPLOAD_PREFIX =
            "uploads/";

    /**
     * Supported MIME types.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "application/pdf"
            );

    /**
     * AWS S3 client used for object operations.
     */
    private final S3Client s3Client;

    /**
     * AWS S3 presigner used to generate temporary URLs.
     */
    private final S3Presigner s3Presigner;

    /**
     * Name of the configured S3 bucket.
     */
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Uploads a file to Amazon S3.
     *
     * <p>
     * The file is validated before being uploaded. A unique
     * object key is generated so that files with the same
     * original name do not overwrite each other.
     * </p>
     *
     * @param file multipart file received from the client
     * @return information about the uploaded file
     */
    @Override
    @Auditable(
            action = "UPLOAD_FILE",
            entity = "STORAGE"
    )
    public FileUploadResponse uploadFile(
            MultipartFile file
    ) {

        validateFile(file);

        String originalFileName =
                file.getOriginalFilename();

        String objectKey =
                generateObjectKey(originalFileName);

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

            return FileUploadResponse.builder()
                    .fileName(originalFileName)
                    .objectKey(objectKey)
                    .fileUrl(null)
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
     * Generates a temporary presigned URL for accessing
     * an object stored inside the private S3 bucket.
     *
     * <p>
     * The generated URL remains valid for 15 minutes.
     * After expiration, a new URL must be generated.
     * </p>
     *
     * @param objectKey S3 object key
     * @return temporary presigned URL
     */
    @Override
    public String generatePresignedUrl(
            String objectKey
    ) {

        validateObjectKey(objectKey);

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofMinutes(15)
                        )
                        .getObjectRequest(
                                getObjectRequest
                        )
                        .build();

        return s3Presigner
                .presignGetObject(
                        presignRequest
                )
                .url()
                .toString();
    }

    /**
     * Validates an uploaded file before storing it.
     *
     * @param file uploaded multipart file
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
     * Validates an S3 object key.
     *
     * <p>
     * Only objects belonging to the application's upload
     * prefix can be requested through this service.
     * </p>
     *
     * @param objectKey S3 object key
     */
    private void validateObjectKey(
            String objectKey
    ) {

        if (objectKey == null ||
                objectKey.isBlank()) {

            throw new BadRequestException(
                    "Object key is required"
            );
        }

        if (!objectKey.startsWith(
                UPLOAD_PREFIX
        )) {

            throw new BadRequestException(
                    "Invalid object key"
            );
        }
    }

    /**
     * Generates a unique S3 object key while preserving
     * the original file extension.
     *
     * @param originalFileName original uploaded file name
     * @return generated S3 object key
     */
    private String generateObjectKey(
            String originalFileName
    ) {

        String extension = "";

        int lastDot =
                originalFileName.lastIndexOf('.');

        if (lastDot >= 0 &&
                lastDot <
                        originalFileName.length() - 1) {

            extension =
                    originalFileName
                            .substring(lastDot)
                            .toLowerCase();
        }

        return UPLOAD_PREFIX
                + UUID.randomUUID()
                + extension;
    }
}