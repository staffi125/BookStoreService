package com.spring.project.controller;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.service.BasketService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/basket")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class BasketController {

    private final BasketService basketService;

    @GetMapping
    public List<BookItemDTO> getBasket(HttpSession session) {
        return basketService.getItems(session);
    }

    @PostMapping("/items")
    public List<BookItemDTO> addItem(HttpSession session, @Valid @RequestBody BookItemDTO item) {
        basketService.addItem(session, item.getBookName(), item.getQuantity());
        return basketService.getItems(session);
    }

    @DeleteMapping("/items")
    public List<BookItemDTO> removeItem(HttpSession session, @RequestParam @NotBlank String bookName) {
        basketService.removeItem(session, bookName);
        return basketService.getItems(session);
    }

    @DeleteMapping
    public void clearBasket(HttpSession session) {
        basketService.clear(session);
    }
}
