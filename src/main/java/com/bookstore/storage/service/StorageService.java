package com.bookstore.storage.service;

import com.bookstore.storage.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Defines application-level file storage operations.
 *
 * <p>
 * The application depends on this interface rather than directly
 * depending on Amazon S3 implementation details.
 * </p>
 */
public interface StorageService {

    /**
     * Uploads a file to object storage.
     *
     * @param file file received from the client
     * @return information about the uploaded file
     */
    FileUploadResponse uploadFile(
            MultipartFile file
    );

    /**
     * Generates a temporary URL for accessing a private
     * object stored in object storage.
     *
     * @param objectKey unique object key stored in S3
     * @return temporary presigned URL
     */
    String generatePresignedUrl(
            String objectKey
    );
}