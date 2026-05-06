package com.spring.project.controller;

import com.spring.project.dto.BookDTO;
import com.spring.project.service.BookPaging;
import com.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public Page<BookDTO> getBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(defaultValue = "0") int page) {
        return bookService.searchBooks(q, minPrice, maxPrice, minPages, maxPages, BookPaging.pageable(page));
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public BookDTO getBookByName(@PathVariable String name) {
        return bookService.getBookByName(name);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public BookDTO addBook(@RequestBody @Valid BookDTO book) {
        return bookService.addBook(book);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public BookDTO updateBookByName(@PathVariable String name, @RequestBody @Valid BookDTO book) {
        return bookService.updateBookByName(name, book);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void deleteBookByName(@PathVariable String name) {
        bookService.deleteBookByName(name);
    }
}
