package com.bookstore.storage.controller;

import com.bookstore.storage.dto.FileUploadResponse;
import com.bookstore.storage.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                fileUploadController
                        )
                        .build();
    }

    @Test
    void uploadFile_shouldReturnCreated() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "cover.png",
                        MediaType.IMAGE_PNG_VALUE,
                        "test-image-content".getBytes()
                );

        FileUploadResponse response =
                FileUploadResponse.builder()
                        .fileName("cover.png")
                        .objectKey("abc123.png")
                        .fileUrl(
                                "https://example.com/abc123.png"
                        )
                        .contentType(
                                MediaType.IMAGE_PNG_VALUE
                        )
                        .size(
                                "test-image-content"
                                        .getBytes()
                                        .length
                        )
                        .build();

        when(
                storageService.uploadFile(
                        any()
                )
        ).thenReturn(response);

        mockMvc.perform(
                        multipart(
                                "/api/storage/upload"
                        )
                                .file(file)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "File uploaded successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.fileName")
                                .value("cover.png")
                )
                .andExpect(
                        jsonPath("$.data.objectKey")
                                .value("abc123.png")
                )
                .andExpect(
                        jsonPath("$.data.fileUrl")
                                .value(
                                        "https://example.com/abc123.png"
                                )
                )
                .andExpect(
                        jsonPath("$.data.contentType")
                                .value(
                                        MediaType.IMAGE_PNG_VALUE
                                )
                );

        verify(storageService)
                .uploadFile(any());
    }

    @Test
    void uploadFile_shouldRejectMissingFile()
            throws Exception {

        mockMvc.perform(
                        multipart(
                                "/api/storage/upload"
                        )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }
}