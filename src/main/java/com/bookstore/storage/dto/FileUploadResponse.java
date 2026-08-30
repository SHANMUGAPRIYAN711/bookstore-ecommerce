package com.bookstore.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO returned after a file is successfully uploaded.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * Original name of the uploaded file.
     */
    private String fileName;

    /**
     * Unique object key stored in S3.
     */
    private String objectKey;

    /**
     * URL used to access the uploaded file.
     */
    private String fileUrl;

    /**
     * MIME type of the uploaded file.
     */
    private String contentType;

    /**
     * Size of the uploaded file in bytes.
     */
    private long size;
}