package com.bookstore.inventory.controller;

import com.bookstore.common.constants.ApiConstants;
import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.UpdateInventoryRequest;
import com.bookstore.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller responsible for book inventory operations.
 */
@RestController
@RequestMapping(ApiConstants.INVENTORY_PATH)
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Retrieves the current inventory information of a book.
     *
     * @param bookId identifier of the book
     * @return current inventory information
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable UUID bookId) {

        return ResponseEntity.ok(
                inventoryService.getInventory(bookId)
        );
    }

    /**
     * Updates the stock quantity to an exact value.
     *
     * @param bookId identifier of the book
     * @param request request containing the new stock quantity
     * @return updated inventory information
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable UUID bookId,
            @Valid @RequestBody UpdateInventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.updateStock(
                        bookId,
                        request
                )
        );
    }

    /**
     * Increases the stock quantity of a book.
     *
     * @param bookId identifier of the book
     * @param quantity quantity to add
     * @return updated inventory information
     */
    @PatchMapping("/{bookId}/increase")
    public ResponseEntity<InventoryResponse> increaseStock(
            @PathVariable UUID bookId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.increaseStock(
                        bookId,
                        quantity
                )
        );
    }

    /**
     * Decreases the stock quantity of a book.
     *
     * @param bookId identifier of the book
     * @param quantity quantity to remove
     * @return updated inventory information
     */
    @PatchMapping("/{bookId}/decrease")
    public ResponseEntity<InventoryResponse> decreaseStock(
            @PathVariable UUID bookId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.decreaseStock(
                        bookId,
                        quantity
                )
        );
    }

    /**
     * Checks whether sufficient stock is available for a requested quantity.
     *
     * @param bookId identifier of the book
     * @param quantity required quantity
     * @return true when sufficient stock is available
     */
    @GetMapping("/{bookId}/sufficient")
    public ResponseEntity<Boolean> hasSufficientStock(
            @PathVariable UUID bookId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.hasSufficientStock(
                        bookId,
                        quantity
                )
        );
    }

    /**
     * Checks whether a book is currently available for purchase.
     *
     * @param bookId identifier of the book
     * @return true when the book has stock available
     */
    @GetMapping("/{bookId}/availability")
    public ResponseEntity<Boolean> isAvailable(
            @PathVariable UUID bookId) {

        return ResponseEntity.ok(
                inventoryService.isAvailable(bookId)
        );
    }
}