package com.bookstore.storage.controller;

import com.bookstore.common.dto.ApiResponse;
import com.bookstore.storage.dto.FileUploadResponse;
import com.bookstore.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller responsible for file storage operations.
 *
 * <p>
 * Provides endpoints for uploading files to Amazon S3 and
 * generating temporary URLs for accessing private objects.
 * </p>
 */
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class FileUploadController {

    /**
     * Application-level storage service.
     */
    private final StorageService storageService;

    /**
     * Uploads a file to AWS S3.
     *
     * @param file multipart file supplied by the client
     * @return information about the uploaded file
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
            ApiResponse<FileUploadResponse>
            > uploadFile(
            @RequestParam("file")
            MultipartFile file
    ) {

        FileUploadResponse response =
                storageService.uploadFile(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse
                                .<FileUploadResponse>builder()
                                .success(true)
                                .message(
                                        "File uploaded successfully"
                                )
                                .data(response)
                                .build()
                );
    }

    /**
     * Generates a temporary presigned URL for accessing
     * a private S3 object.
     *
     * @param objectKey S3 object key
     * @return temporary presigned URL
     */
    @GetMapping("/url")
    public ResponseEntity<
            ApiResponse<String>
            > generatePresignedUrl(
            @RequestParam("objectKey")
            String objectKey
    ) {

        String presignedUrl =
                storageService.generatePresignedUrl(
                        objectKey
                );

        return ResponseEntity.ok(
                ApiResponse
                        .<String>builder()
                        .success(true)
                        .message(
                                "Presigned URL generated successfully"
                        )
                        .data(presignedUrl)
                        .build()
        );
    }
}