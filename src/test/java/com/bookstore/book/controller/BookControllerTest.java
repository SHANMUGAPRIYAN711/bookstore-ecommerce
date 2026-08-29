package com.bookstore.book.controller;

import com.bookstore.book.dto.BookCreateRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.dto.BookSearchRequest;
import com.bookstore.book.dto.BookUpdateRequest;
import com.bookstore.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.bookstore.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID bookId = UUID.randomUUID();

    private BookResponse createBookResponse() {

        return BookResponse.builder()
                .id(bookId)
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .description("Software craftsmanship")
                .category("Programming")
                .price(new BigDecimal("45.00"))
                .stockQuantity(10)
                .imageUrl("https://example.com/clean-code.jpg")
                .averageRating(4.5)
                .reviewCount(10L)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ============================================================
    // CREATE BOOK
    // ============================================================

    @Test
    void createBook_shouldReturnCreated() throws Exception {

        BookCreateRequest request = BookCreateRequest.builder()
                .title("Clean Code")
                .isbn("9780132350884")
                .author("Robert Martin")
                .description("Software craftsmanship")
                .category("Programming")
                .price(new BigDecimal("45.00"))
                .stockQuantity(10)
                .imageUrl("https://example.com/clean-code.jpg")
                .build();

        BookResponse response = createBookResponse();

        when(bookService.createBook(any(BookCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.price").value(45.00))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.active").value(true));

        verify(bookService).createBook(any(BookCreateRequest.class));
    }

    @Test
    void createBook_shouldRejectInvalidRequest() throws Exception {

        BookCreateRequest request = BookCreateRequest.builder()
                .title("")
                .isbn("")
                .author("")
                .category("")
                .price(null)
                .stockQuantity(null)
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookService, never())
                .createBook(any(BookCreateRequest.class));
    }

    // ============================================================
    // GET BOOK BY ID
    // ============================================================

    @Test
    void getBookById_shouldReturnBook() throws Exception {

        BookResponse response = createBookResponse();

        when(bookService.getBookById(bookId))
                .thenReturn(response);

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.category").value("Programming"));

        verify(bookService).getBookById(bookId);
    }

    @Test
    void getBookById_shouldRejectInvalidUuid() throws Exception {

        mockMvc.perform(get("/api/books/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(bookService, never())
                .getBookById(any());
    }

    // ============================================================
    // GET ALL BOOKS
    // ============================================================

    @Test
    void getAllBooks_shouldReturnPaginatedBooks() throws Exception {

        BookResponse response = createBookResponse();

        Page<BookResponse> page =
                new PageImpl<>(List.of(response));

        when(bookService.getAllBooks(
                eq(0),
                eq(10),
                eq("title"),
                eq("ASC")
        )).thenReturn(page);

        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Clean Code"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));

        verify(bookService).getAllBooks(
                0,
                10,
                "title",
                "ASC"
        );
    }

    @Test
    void getAllBooks_shouldUseDefaultPagination() throws Exception {

        Page<BookResponse> page =
                new PageImpl<>(List.of(createBookResponse()));

        when(bookService.getAllBooks(
                eq(0),
                eq(10),
                isNull(),
                isNull()
        )).thenReturn(page);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());

        verify(bookService).getAllBooks(
                0,
                10,
                null,
                null
        );
    }

    // ============================================================
    // SEARCH BOOKS
    // ============================================================

    @Test
    void searchBooks_shouldReturnMatchingBooks() throws Exception {

        BookSearchRequest request = BookSearchRequest.builder()
                .keyword("clean")
                .category("Programming")
                .minPrice(new BigDecimal("20.00"))
                .maxPrice(new BigDecimal("100.00"))
                .page(0)
                .size(20)
                .sortBy("title")
                .sortDirection("ASC")
                .build();

        Page<BookResponse> page =
                new PageImpl<>(List.of(createBookResponse()));

        when(bookService.searchBooks(any(BookSearchRequest.class)))
                .thenReturn(page);

        mockMvc.perform(post("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Clean Code"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));

        verify(bookService)
                .searchBooks(any(BookSearchRequest.class));
    }

    @Test
    void searchBooks_shouldRejectInvalidPageSize() throws Exception {

        BookSearchRequest request = BookSearchRequest.builder()
                .keyword("clean")
                .page(0)
                .size(101)
                .build();

        mockMvc.perform(post("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookService, never())
                .searchBooks(any(BookSearchRequest.class));
    }

    // ============================================================
    // ACTIVE BOOKS
    // ============================================================

    @Test
    void getActiveBooks_shouldReturnActiveBooks() throws Exception {

        Page<BookResponse> page =
                new PageImpl<>(List.of(createBookResponse()));

        when(bookService.getActiveBooks(
                eq(0),
                eq(10),
                eq("createdAt"),
                eq("DESC")
        )).thenReturn(page);

        mockMvc.perform(get("/api/books/active")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].active")
                        .value(true));

        verify(bookService).getActiveBooks(
                0,
                10,
                "createdAt",
                "DESC"
        );
    }

    // ============================================================
    // AVAILABLE BOOKS
    // ============================================================

    @Test
    void getAvailableBooks_shouldReturnAvailableBooks() throws Exception {

        Page<BookResponse> page =
                new PageImpl<>(List.of(createBookResponse()));

        when(bookService.getAvailableBooks(
                eq(0),
                eq(10),
                eq("price"),
                eq("ASC")
        )).thenReturn(page);

        mockMvc.perform(get("/api/books/available")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "price")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].stockQuantity")
                        .value(10));

        verify(bookService).getAvailableBooks(
                0,
                10,
                "price",
                "ASC"
        );
    }

    // ============================================================
    // UPDATE BOOK
    // ============================================================

    @Test
    void updateBook_shouldReturnUpdatedBook() throws Exception {

        BookUpdateRequest request = BookUpdateRequest.builder()
                .title("Clean Code Updated")
                .author("Robert C. Martin")
                .category("Software Engineering")
                .price(new BigDecimal("50.00"))
                .active(true)
                .build();

        BookResponse response = createBookResponse();
        response.setTitle("Clean Code Updated");
        response.setPrice(new BigDecimal("50.00"));

        when(bookService.updateBook(
                eq(bookId),
                any(BookUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Clean Code Updated"))
                .andExpect(jsonPath("$.price")
                        .value(50.00));

        verify(bookService).updateBook(
                eq(bookId),
                any(BookUpdateRequest.class)
        );
    }

    @Test
    void updateBook_shouldRejectInvalidUuid() throws Exception {

        BookUpdateRequest request = BookUpdateRequest.builder()
                .title("Updated Book")
                .build();

        mockMvc.perform(put("/api/books/{id}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookService, never())
                .updateBook(any(), any(BookUpdateRequest.class));
    }

    // ============================================================
    // DELETE BOOK
    // ============================================================

    @Test
    void deleteBook_shouldReturnNoContent() throws Exception {

        doNothing()
                .when(bookService)
                .deleteBook(bookId);

        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(bookId);
    }

    @Test
    void deleteBook_shouldRejectInvalidUuid() throws Exception {

        mockMvc.perform(delete("/api/books/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(bookService, never())
                .deleteBook(any());
    }
}