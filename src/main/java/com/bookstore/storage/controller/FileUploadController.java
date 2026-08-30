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
 * REST controller responsible for file upload operations.
 */
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;

    /**
     * Uploads a file to AWS S3.
     *
     * @param file multipart file supplied by the client
     * @return uploaded file information
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
                        ApiResponse.<FileUploadResponse>builder()
                                .success(true)
                                .message(
                                        "File uploaded successfully"
                                )
                                .data(response)
                                .build()
                );
    }
}