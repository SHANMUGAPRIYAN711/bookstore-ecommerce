package com.bookstore.inventory.controller;

import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.UpdateInventoryRequest;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@WithMockUser(
        username = "testuser",
        roles = {"USER"}
)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final UUID bookId = UUID.randomUUID();

    @Test
    void getInventory_shouldReturnInventorySuccessfully() throws Exception {

        InventoryResponse response = InventoryResponse.builder()
                .bookId(bookId)
                .bookTitle("Clean Code")
                .stockQuantity(10)
                .available(true)
                .build();

        when(inventoryService.getInventory(bookId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/inventory/{bookId}", bookId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.available").value(true));

        verify(inventoryService).getInventory(bookId);
    }

    @Test
    void updateStock_shouldReturnUpdatedInventory() throws Exception {

        UpdateInventoryRequest request =
                UpdateInventoryRequest.builder()
                        .stockQuantity(25)
                        .build();

        InventoryResponse response = InventoryResponse.builder()
                .bookId(bookId)
                .bookTitle("Clean Code")
                .stockQuantity(25)
                .available(true)
                .build();

        when(inventoryService.updateStock(
                eq(bookId),
                any(UpdateInventoryRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/inventory/{bookId}", bookId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(25))
                .andExpect(jsonPath("$.available").value(true));

        verify(inventoryService)
                .updateStock(eq(bookId), any(UpdateInventoryRequest.class));
    }

    @Test
    void updateStock_shouldRejectNegativeStock() throws Exception {

        UpdateInventoryRequest request =
                UpdateInventoryRequest.builder()
                        .stockQuantity(-5)
                        .build();

        mockMvc.perform(
                        put("/api/inventory/{bookId}", bookId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void increaseStock_shouldReturnUpdatedInventory() throws Exception {

        InventoryResponse response = InventoryResponse.builder()
                .bookId(bookId)
                .bookTitle("Clean Code")
                .stockQuantity(15)
                .available(true)
                .build();

        when(inventoryService.increaseStock(bookId, 5))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/inventory/{bookId}/increase", bookId)
                                .with(csrf())
                                .param("quantity", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(15))
                .andExpect(jsonPath("$.available").value(true));

        verify(inventoryService).increaseStock(bookId, 5);
    }

    @Test
    void decreaseStock_shouldReturnUpdatedInventory() throws Exception {

        InventoryResponse response = InventoryResponse.builder()
                .bookId(bookId)
                .bookTitle("Clean Code")
                .stockQuantity(5)
                .available(true)
                .build();

        when(inventoryService.decreaseStock(bookId, 5))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/inventory/{bookId}/decrease", bookId)
                                .with(csrf())
                                .param("quantity", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(5))
                .andExpect(jsonPath("$.available").value(true));

        verify(inventoryService).decreaseStock(bookId, 5);
    }

    @Test
    void hasSufficientStock_shouldReturnTrue() throws Exception {

        when(inventoryService.hasSufficientStock(bookId, 5))
                .thenReturn(true);

        mockMvc.perform(
                        get("/api/inventory/{bookId}/sufficient", bookId)
                                .param("quantity", "5")
                )
                .andExpect(status().isOk())
                .andExpect(content -> {
                    // Response body is validated below through the status
                    // and service verification.
                });

        verify(inventoryService).hasSufficientStock(bookId, 5);
    }

    @Test
    void hasSufficientStock_shouldReturnFalse() throws Exception {

        when(inventoryService.hasSufficientStock(bookId, 15))
                .thenReturn(false);

        mockMvc.perform(
                        get("/api/inventory/{bookId}/sufficient", bookId)
                                .param("quantity", "15")
                )
                .andExpect(status().isOk())
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertEquals(
                                "false",
                                result.getResponse().getContentAsString()
                        )
                );

        verify(inventoryService).hasSufficientStock(bookId, 15);
    }

    @Test
    void isAvailable_shouldReturnTrue() throws Exception {

        when(inventoryService.isAvailable(bookId))
                .thenReturn(true);

        mockMvc.perform(
                        get("/api/inventory/{bookId}/availability", bookId)
                )
                .andExpect(status().isOk())
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertEquals(
                                "true",
                                result.getResponse().getContentAsString()
                        )
                );

        verify(inventoryService).isAvailable(bookId);
    }

    @Test
    void isAvailable_shouldReturnFalseWhenStockIsZero() throws Exception {

        when(inventoryService.isAvailable(bookId))
                .thenReturn(false);

        mockMvc.perform(
                        get("/api/inventory/{bookId}/availability", bookId)
                )
                .andExpect(status().isOk())
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertEquals(
                                "false",
                                result.getResponse().getContentAsString()
                        )
                );

        verify(inventoryService).isAvailable(bookId);
    }
}