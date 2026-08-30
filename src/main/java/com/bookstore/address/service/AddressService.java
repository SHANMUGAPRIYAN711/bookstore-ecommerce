package com.bookstore.address.service;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressResponse createAddress(
            UUID userId,
            AddressRequest request
    );

    List<AddressResponse> getUserAddresses(
            UUID userId
    );

    AddressResponse getAddress(
            UUID userId,
            UUID addressId
    );

    AddressResponse updateAddress(
            UUID userId,
            UUID addressId,
            AddressRequest request
    );

    void deleteAddress(
            UUID userId,
            UUID addressId
    );
}