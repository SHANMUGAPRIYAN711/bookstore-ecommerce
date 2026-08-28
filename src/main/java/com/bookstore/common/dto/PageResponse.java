package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Generic pagination response used by collection-based REST APIs.
 *
 * @param <T> type of records contained in the page
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * Records contained in the current page.
     */
    private List<T> content;

    /**
     * Zero-based page number.
     */
    private int page;

    /**
     * Number of records per page.
     */
    private int size;

    /**
     * Total number of matching records.
     */
    private long totalElements;

    /**
     * Total number of available pages.
     */
    private int totalPages;

    /**
     * Indicates whether this is the final page.
     */
    private boolean last;
}