package com.bookstore.inventory.service;

import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.UpdateInventoryRequest;

import java.util.UUID;

/**
 * Defines business operations for managing book inventory.
 */
public interface InventoryService {

    /**
     * Retrieves the current inventory information of a book.
     *
     * @param bookId identifier of the book
     * @return current inventory information
     */
    InventoryResponse getInventory(UUID bookId);

    /**
     * Updates the stock quantity to an exact value.
     *
     * @param bookId identifier of the book
     * @param request request containing the new stock quantity
     * @return updated inventory information
     */
    InventoryResponse updateStock(
            UUID bookId,
            UpdateInventoryRequest request
    );

    /**
     * Increases the current stock quantity.
     *
     * @param bookId identifier of the book
     * @param quantity quantity to add
     * @return updated inventory information
     */
    InventoryResponse increaseStock(
            UUID bookId,
            Integer quantity
    );

    /**
     * Decreases the current stock quantity.
     *
     * <p>
     * The operation must fail when the requested quantity is greater
     * than the currently available stock.
     * </p>
     *
     * @param bookId identifier of the book
     * @param quantity quantity to remove
     * @return updated inventory information
     */
    InventoryResponse decreaseStock(
            UUID bookId,
            Integer quantity
    );

    /**
     * Checks whether a book has sufficient stock for the requested quantity.
     *
     * @param bookId identifier of the book
     * @param quantity required quantity
     * @return true when sufficient stock is available
     */
    boolean hasSufficientStock(
            UUID bookId,
            Integer quantity
    );

    /**
     * Checks whether a book is currently available for purchase.
     *
     * @param bookId identifier of the book
     * @return true when stock is greater than zero
     */
    boolean isAvailable(UUID bookId);
}