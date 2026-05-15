package com.spring.project.controller;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.service.BasketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public List<BookItemDTO> getBasket(@AuthenticationPrincipal UserDetails principal) {
        return basketService.getItems(principal.getUsername());
    }

    @PostMapping("/items")
    public List<BookItemDTO> addItem(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody BookItemDTO item) {
        basketService.addItem(principal.getUsername(), item.getBookName(), item.getQuantity());
        return basketService.getItems(principal.getUsername());
    }

    @DeleteMapping("/items")
    public List<BookItemDTO> removeItem(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam @NotBlank String bookName) {
        basketService.removeItem(principal.getUsername(), bookName);
        return basketService.getItems(principal.getUsername());
    }

    @DeleteMapping
    public void clearBasket(@AuthenticationPrincipal UserDetails principal) {
        basketService.clear(principal.getUsername());
    }
}
