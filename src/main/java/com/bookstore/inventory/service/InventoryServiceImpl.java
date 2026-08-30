package com.bookstore.inventory.service;

import com.bookstore.audit.annotation.Auditable;
import com.bookstore.book.entity.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.UpdateInventoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the inventory business operations.
 *
 * <p>
 * Inventory quantity is maintained on the Book entity through the
 * stockQuantity field. This service is responsible for safely reading
 * and modifying that quantity.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final BookRepository bookRepository;

    /**
     * Retrieves the current inventory information of a book.
     */
    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(UUID bookId) {

        Book book = findBook(bookId);

        return mapToResponse(book);
    }

    /**
     * Updates the stock quantity to an exact value.
     */
    @Auditable(
            action = "UPDATE_STOCK",
            entity = "INVENTORY"
    )
    @Override
    public InventoryResponse updateStock(
            UUID bookId,
            UpdateInventoryRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Inventory update request is required"
            );
        }

        if (request.getStockQuantity() == null) {
            throw new BadRequestException(
                    "Stock quantity is required"
            );
        }

        if (request.getStockQuantity() < 0) {
            throw new BadRequestException(
                    "Stock quantity cannot be negative"
            );
        }

        Book book = findBook(bookId);

        book.setStockQuantity(
                request.getStockQuantity()
        );

        Book savedBook =
                bookRepository.save(book);

        return mapToResponse(savedBook);
    }

    /**
     * Increases the current stock quantity.
     */
    @Auditable(
            action = "INCREASE_STOCK",
            entity = "INVENTORY"
    )
    @Override
    public InventoryResponse increaseStock(
            UUID bookId,
            Integer quantity) {

        validateQuantity(quantity);

        Book book = findBook(bookId);

        int currentStock =
                book.getStockQuantity();

        int updatedStock =
                currentStock + quantity;

        book.setStockQuantity(
                updatedStock
        );

        Book savedBook =
                bookRepository.save(book);

        return mapToResponse(savedBook);
    }

    /**
     * Decreases the current stock quantity.
     *
     * <p>
     * The operation is rejected when the requested quantity is greater
     * than the currently available stock.
     * </p>
     */
    @Auditable(
            action = "DECREASE_STOCK",
            entity = "INVENTORY"
    )
    @Override
    public InventoryResponse decreaseStock(
            UUID bookId,
            Integer quantity) {

        validateQuantity(quantity);

        Book book = findBook(bookId);

        int currentStock =
                book.getStockQuantity();

        if (quantity > currentStock) {
            throw new BadRequestException(
                    "Insufficient stock. Available stock: "
                            + currentStock
            );
        }

        int updatedStock =
                currentStock - quantity;

        book.setStockQuantity(
                updatedStock
        );

        Book savedBook =
                bookRepository.save(book);

        return mapToResponse(savedBook);
    }

    /**
     * Checks whether a book has sufficient stock for the requested quantity.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(
            UUID bookId,
            Integer quantity) {

        validateQuantity(quantity);

        Book book = findBook(bookId);

        return book.getStockQuantity() >= quantity;
    }

    /**
     * Checks whether a book currently has stock available.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(UUID bookId) {

        Book book = findBook(bookId);

        return book.getStockQuantity() > 0;
    }

    /**
     * Finds a book by its identifier.
     *
     * <p>
     * A ResourceNotFoundException is thrown when the book does not exist.
     * </p>
     */
    private Book findBook(UUID bookId) {

        if (bookId == null) {
            throw new BadRequestException(
                    "Book ID is required"
            );
        }

        return bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: "
                                        + bookId
                        )
                );
    }

    /**
     * Validates a stock adjustment quantity.
     */
    private void validateQuantity(Integer quantity) {

        if (quantity == null) {
            throw new BadRequestException(
                    "Quantity is required"
            );
        }

        if (quantity < 0) {
            throw new BadRequestException(
                    "Quantity cannot be negative"
            );
        }
    }

    /**
     * Converts a Book entity into an InventoryResponse.
     */
    private InventoryResponse mapToResponse(Book book) {

        return InventoryResponse.builder()
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .stockQuantity(
                        book.getStockQuantity()
                )
                .available(
                        book.getStockQuantity() > 0
                )
                .build();
    }
}