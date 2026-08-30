package com.bookstore.address.controller;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;
import com.bookstore.address.service.AddressService;
import com.bookstore.common.constants.ApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    private UUID userId;
    private UUID addressId;

    private AddressRequest request;
    private AddressResponse response;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        request = AddressRequest.builder()
                .addressLine1("123 MG Road")
                .addressLine2("Near Metro Station")
                .city("Bangalore")
                .state("Karnataka")
                .postalCode("560001")
                .country("India")
                .defaultAddress(true)
                .build();

        response = AddressResponse.builder()
                .id(addressId)
                .addressLine1("123 MG Road")
                .addressLine2("Near Metro Station")
                .city("Bangalore")
                .state("Karnataka")
                .postalCode("560001")
                .country("India")
                .defaultAddress(true)
                .build();
    }

    // ============================================================
    // CREATE ADDRESS
    // ============================================================

    @Test
    void createAddress_shouldReturnCreatedAddress() throws Exception {

        when(addressService.createAddress(
                eq(userId),
                any(AddressRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post(ApiConstants.ADDRESSES_PATH + "/" + userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is2xxSuccessful());
    }

    // ============================================================
    // GET ALL ADDRESSES
    // ============================================================

    @Test
    void getUserAddresses_shouldReturnAddresses() throws Exception {

        when(addressService.getUserAddresses(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get(ApiConstants.ADDRESSES_PATH + "/" + userId)
                )
                .andExpect(status().isOk());
    }

    // ============================================================
    // GET SINGLE ADDRESS
    // ============================================================

    @Test
    void getAddress_shouldReturnAddress() throws Exception {

        when(addressService.getAddress(
                userId,
                addressId
        )).thenReturn(response);

        mockMvc.perform(
                        get(
                                ApiConstants.ADDRESSES_PATH
                                        + "/"
                                        + userId
                                        + "/"
                                        + addressId
                        )
                )
                .andExpect(status().isOk());
    }

    // ============================================================
    // UPDATE ADDRESS
    // ============================================================

    @Test
    void updateAddress_shouldReturnUpdatedAddress() throws Exception {

        when(addressService.updateAddress(
                eq(userId),
                eq(addressId),
                any(AddressRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                ApiConstants.ADDRESSES_PATH
                                        + "/"
                                        + userId
                                        + "/"
                                        + addressId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is2xxSuccessful());
    }

    // ============================================================
    // DELETE ADDRESS
    // ============================================================

    @Test
    void deleteAddress_shouldDeleteAddress() throws Exception {

        doNothing().when(addressService)
                .deleteAddress(
                        userId,
                        addressId
                );

        mockMvc.perform(
                        delete(
                                ApiConstants.ADDRESSES_PATH
                                        + "/"
                                        + userId
                                        + "/"
                                        + addressId
                        )
                )
                .andExpect(status().is2xxSuccessful());
    }
}