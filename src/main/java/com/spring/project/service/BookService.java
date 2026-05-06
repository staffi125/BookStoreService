package com.spring.project.service;

import com.spring.project.dto.BookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {

    List<BookDTO> getAllBooks();

    /**
     * Search by title, author, or genre (case-insensitive), combined with optional price and page-count bounds.
     */
    Page<BookDTO> searchBooks(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minPages,
            Integer maxPages,
            Pageable pageable);

    BookDTO getBookByName(String name);

    BookDTO updateBookByName(String name, BookDTO book);

    void deleteBookByName(String name);

    BookDTO addBook(BookDTO book);
}
