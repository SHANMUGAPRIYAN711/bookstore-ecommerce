package com.bookstore.address.service;

import com.bookstore.address.dto.AddressRequest;
import com.bookstore.address.dto.AddressResponse;
import com.bookstore.address.entity.Address;
import com.bookstore.address.repository.AddressRepository;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private UUID userId;
    private UUID addressId;

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        user = User.builder()
                .firstName("Ishaan")
                .lastName("Singh")
                .email("ishaan@gmail.com")
                .password("encodedPassword")
                .build();

        user.setId(userId);

        address = Address.builder()
                .user(user)
                .addressLine1("123 MG Road")
                .addressLine2("Near Metro Station")
                .city("Bangalore")
                .state("Karnataka")
                .postalCode("560001")
                .country("India")
                .defaultAddress(true)
                .build();

        address.setId(addressId);
    }

    // ============================================================
    // CREATE ADDRESS
    // ============================================================

    @Test
    void createAddress_shouldCreateAddressSuccessfully() {

        AddressRequest request =
                AddressRequest.builder()
                        .addressLine1("123 MG Road")
                        .addressLine2("Near Metro Station")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560001")
                        .country("India")
                        .defaultAddress(true)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository
                .findByUserIdAndDefaultAddressTrue(userId))
                .thenReturn(Optional.empty());

        when(addressRepository.save(any(Address.class)))
                .thenReturn(address);

        AddressResponse response =
                addressService.createAddress(
                        userId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                addressId,
                response.getId()
        );

        assertEquals(
                "123 MG Road",
                response.getAddressLine1()
        );

        assertEquals(
                "Near Metro Station",
                response.getAddressLine2()
        );

        assertEquals(
                "Bangalore",
                response.getCity()
        );

        assertEquals(
                "Karnataka",
                response.getState()
        );

        assertEquals(
                "560001",
                response.getPostalCode()
        );

        assertEquals(
                "India",
                response.getCountry()
        );

        assertEquals(
                true,
                response.isDefaultAddress()
        );

        verify(userRepository)
                .findById(userId);

        verify(addressRepository)
                .findByUserIdAndDefaultAddressTrue(userId);

        verify(addressRepository)
                .save(any(Address.class));
    }

    // ============================================================
    // CREATE ADDRESS - USER NOT FOUND
    // ============================================================

    @Test
    void createAddress_shouldThrowExceptionWhenUserNotFound() {

        AddressRequest request =
                AddressRequest.builder()
                        .addressLine1("123 MG Road")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560001")
                        .country("India")
                        .defaultAddress(false)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> addressService.createAddress(
                        userId,
                        request
                )
        );

        verify(userRepository)
                .findById(userId);

        verify(addressRepository, never())
                .save(any(Address.class));
    }

    // ============================================================
    // CREATE DEFAULT ADDRESS
    // ============================================================

    @Test
    void createAddress_shouldRemoveExistingDefaultAddress() {

        Address existingAddress =
                Address.builder()
                        .user(user)
                        .addressLine1("Old Address")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560002")
                        .country("India")
                        .defaultAddress(true)
                        .build();

        existingAddress.setId(UUID.randomUUID());

        AddressRequest request =
                AddressRequest.builder()
                        .addressLine1("123 MG Road")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560001")
                        .country("India")
                        .defaultAddress(true)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(addressRepository
                .findByUserIdAndDefaultAddressTrue(userId))
                .thenReturn(Optional.of(existingAddress));

        when(addressRepository.save(any(Address.class)))
                .thenReturn(address);

        addressService.createAddress(
                userId,
                request
        );

        assertEquals(
                false,
                existingAddress.isDefaultAddress()
        );

        verify(addressRepository)
                .save(existingAddress);

        verify(addressRepository, times(2))
                .save(any(Address.class));
    }

    // ============================================================
    // GET USER ADDRESSES
    // ============================================================

    @Test
    void getUserAddresses_shouldReturnUserAddresses() {

        Address secondAddress =
                Address.builder()
                        .user(user)
                        .addressLine1("456 Brigade Road")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560025")
                        .country("India")
                        .defaultAddress(false)
                        .build();

        secondAddress.setId(UUID.randomUUID());

        when(addressRepository.findByUserId(userId))
                .thenReturn(
                        List.of(
                                address,
                                secondAddress
                        )
                );

        List<AddressResponse> response =
                addressService.getUserAddresses(userId);

        assertNotNull(response);

        assertEquals(
                2,
                response.size()
        );

        assertEquals(
                addressId,
                response.get(0).getId()
        );

        assertEquals(
                "123 MG Road",
                response.get(0).getAddressLine1()
        );

        assertEquals(
                "456 Brigade Road",
                response.get(1).getAddressLine1()
        );

        verify(addressRepository)
                .findByUserId(userId);
    }

    // ============================================================
    // GET SINGLE ADDRESS
    // ============================================================

    @Test
    void getAddress_shouldReturnAddress() {

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.of(address)
        );

        AddressResponse response =
                addressService.getAddress(
                        userId,
                        addressId
                );

        assertNotNull(response);

        assertEquals(
                addressId,
                response.getId()
        );

        assertEquals(
                "123 MG Road",
                response.getAddressLine1()
        );

        assertEquals(
                "Bangalore",
                response.getCity()
        );

        assertEquals(
                "India",
                response.getCountry()
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );
    }

    // ============================================================
    // GET SINGLE ADDRESS - NOT FOUND
    // ============================================================

    @Test
    void getAddress_shouldThrowExceptionWhenAddressNotFound() {

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> addressService.getAddress(
                        userId,
                        addressId
                )
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );
    }

    // ============================================================
    // UPDATE ADDRESS
    // ============================================================

    @Test
    void updateAddress_shouldUpdateAddressSuccessfully() {

        AddressRequest request =
                AddressRequest.builder()
                        .addressLine1("456 Brigade Road")
                        .addressLine2("Apartment 10")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560025")
                        .country("India")
                        .defaultAddress(true)
                        .build();

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.of(address)
        );

        when(addressRepository
                .findByUserIdAndDefaultAddressTrue(userId))
                .thenReturn(
                        Optional.of(address)
                );

        when(addressRepository.save(any(Address.class)))
                .thenReturn(address);

        AddressResponse response =
                addressService.updateAddress(
                        userId,
                        addressId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                "456 Brigade Road",
                address.getAddressLine1()
        );

        assertEquals(
                "Apartment 10",
                address.getAddressLine2()
        );

        assertEquals(
                "Bangalore",
                address.getCity()
        );

        assertEquals(
                "560025",
                address.getPostalCode()
        );

        assertEquals(
                true,
                address.isDefaultAddress()
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );

        verify(addressRepository)
                .save(address);
    }

    // ============================================================
    // UPDATE ADDRESS - NOT FOUND
    // ============================================================

    @Test
    void updateAddress_shouldThrowExceptionWhenAddressNotFound() {

        AddressRequest request =
                AddressRequest.builder()
                        .addressLine1("456 Brigade Road")
                        .city("Bangalore")
                        .state("Karnataka")
                        .postalCode("560025")
                        .country("India")
                        .defaultAddress(false)
                        .build();

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> addressService.updateAddress(
                        userId,
                        addressId,
                        request
                )
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );

        verify(addressRepository, never())
                .save(any(Address.class));
    }

    // ============================================================
    // DELETE ADDRESS
    // ============================================================

    @Test
    void deleteAddress_shouldDeleteAddressSuccessfully() {

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.of(address)
        );

        addressService.deleteAddress(
                userId,
                addressId
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );

        verify(addressRepository)
                .delete(address);
    }

    // ============================================================
    // DELETE ADDRESS - NOT FOUND
    // ============================================================

    @Test
    void deleteAddress_shouldThrowExceptionWhenAddressNotFound() {

        when(addressRepository.findByIdAndUserId(
                addressId,
                userId
        )).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> addressService.deleteAddress(
                        userId,
                        addressId
                )
        );

        verify(addressRepository)
                .findByIdAndUserId(
                        addressId,
                        userId
                );

        verify(addressRepository, never())
                .delete(any(Address.class));
    }
}