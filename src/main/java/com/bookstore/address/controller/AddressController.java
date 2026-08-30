package com.bookstore.address.controller;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;
import com.bookstore.address.service.AddressService;
import com.bookstore.common.constants.ApiConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ADDRESSES_PATH)
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @RequestParam UUID userId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(
                addressService.createAddress(
                        userId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(
            @RequestParam UUID userId) {

        return ResponseEntity.ok(
                addressService.getUserAddresses(
                        userId
                )
        );
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @RequestParam UUID userId,
            @PathVariable UUID addressId) {

        return ResponseEntity.ok(
                addressService.getAddress(
                        userId,
                        addressId
                )
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @RequestParam UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(
                addressService.updateAddress(
                        userId,
                        addressId,
                        request
                )
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @RequestParam UUID userId,
            @PathVariable UUID addressId) {

        addressService.deleteAddress(
                userId,
                addressId
        );

        return ResponseEntity.noContent().build();
    }
}