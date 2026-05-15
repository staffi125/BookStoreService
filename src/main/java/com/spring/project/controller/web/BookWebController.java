package com.spring.project.controller.web;

import com.spring.project.dto.BookDTO;
import com.spring.project.dto.web.BookCardDto;
import com.spring.project.dto.web.BookSearchPageDto;
import com.spring.project.service.BookPaging;
import com.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;

@Controller
@RequestMapping("/app/books")
@RequiredArgsConstructor
public class BookWebController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Page<BookDTO> bookPage = bookService.searchBooks(
                q, minPrice, maxPrice, minPages, maxPages, BookPaging.pageable(page));
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minPages", minPages);
        model.addAttribute("maxPages", maxPages);
        return "books/list";
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    @ResponseBody
    public BookSearchPageDto searchJson(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(defaultValue = "0") int page) {
        Page<BookDTO> bookPage = bookService.searchBooks(
                q, minPrice, maxPrice, minPages, maxPages, BookPaging.pageable(page));
        var content = bookPage.getContent().stream()
                .map(book -> new BookCardDto(
                        book.getName(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getPrice(),
                        book.getPages(),
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/app/books/detail")
                                .queryParam("name", book.getName())
                                .build()
                                .toUriString()))
                .toList();
        return new BookSearchPageDto(
                content,
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.getNumber(),
                bookPage.hasNext(),
                bookPage.hasPrevious());
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public String detail(@RequestParam("name") String name, Model model) {
        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("book", book);
        return "books/detail";
    }
}
