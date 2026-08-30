package com.bookstore.address.service;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;
import com.bookstore.address.entity.Address;
import com.bookstore.address.repository.AddressRepository;
import com.bookstore.audit.annotation.Auditable;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Auditable(
            action = "CREATE_ADDRESS",
            entity = "ADDRESS"
    )
    public AddressResponse createAddress(
            UUID userId,
            AddressRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (request.isDefaultAddress()) {

            addressRepository
                    .findByUserIdAndDefaultAddressTrue(userId)
                    .ifPresent(existing -> {

                        existing.setDefaultAddress(false);

                        addressRepository.save(existing);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .defaultAddress(request.isDefaultAddress())
                .build();

        return mapToResponse(
                addressRepository.save(address)
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(
            action = "GET_ADDRESSES",
            entity = "ADDRESS"
    )
    public List<AddressResponse> getUserAddresses(
            UUID userId) {

        return addressRepository
                .findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Auditable(
            action = "GET_ADDRESS",
            entity = "ADDRESS"
    )
    public AddressResponse getAddress(
            UUID userId,
            UUID addressId) {

        Address address =
                addressRepository
                        .findByIdAndUserId(
                                addressId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Address not found"
                                ));

        return mapToResponse(address);
    }

    @Override
    @Auditable(
            action = "UPDATE_ADDRESS",
            entity = "ADDRESS"
    )
    public AddressResponse updateAddress(
            UUID userId,
            UUID addressId,
            AddressRequest request) {

        Address address =
                addressRepository
                        .findByIdAndUserId(
                                addressId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Address not found"
                                ));

        if (request.isDefaultAddress()) {

            addressRepository
                    .findByUserIdAndDefaultAddressTrue(userId)
                    .ifPresent(existing -> {

                        if (!existing.getId().equals(addressId)) {

                            existing.setDefaultAddress(false);

                            addressRepository.save(existing);
                        }
                    });
        }

        address.setAddressLine1(
                request.getAddressLine1()
        );

        address.setAddressLine2(
                request.getAddressLine2()
        );

        address.setCity(
                request.getCity()
        );

        address.setState(
                request.getState()
        );

        address.setPostalCode(
                request.getPostalCode()
        );

        address.setCountry(
                request.getCountry()
        );

        address.setDefaultAddress(
                request.isDefaultAddress()
        );

        return mapToResponse(
                addressRepository.save(address)
        );
    }

    @Override
    @Auditable(
            action = "DELETE_ADDRESS",
            entity = "ADDRESS"
    )
    public void deleteAddress(
            UUID userId,
            UUID addressId) {

        Address address =
                addressRepository
                        .findByIdAndUserId(
                                addressId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Address not found"
                                ));

        addressRepository.delete(address);
    }

    private AddressResponse mapToResponse(
            Address address) {

        return AddressResponse.builder()
                .id(address.getId())
                .addressLine1(
                        address.getAddressLine1()
                )
                .addressLine2(
                        address.getAddressLine2()
                )
                .city(
                        address.getCity()
                )
                .state(
                        address.getState()
                )
                .postalCode(
                        address.getPostalCode()
                )
                .country(
                        address.getCountry()
                )
                .defaultAddress(
                        address.isDefaultAddress()
                )
                .createdAt(
                        address.getCreatedAt()
                )
                .updatedAt(
                        address.getUpdatedAt()
                )
                .build();
    }
}