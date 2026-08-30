package com.bookstore.storage.service;

import com.bookstore.storage.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Defines application-level file storage operations.
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
}