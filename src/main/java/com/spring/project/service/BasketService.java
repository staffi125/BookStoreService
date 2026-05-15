package com.spring.project.service;

import com.spring.project.dto.BookItemDTO;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface BasketService {

    void addItem(String clientEmail, String bookName, int quantity);

    void removeItem(String clientEmail, String bookName);

    void clear(String clientEmail);

    List<BookItemDTO> getItems(String clientEmail);

    /**
     * One-time import of items stored in the HTTP session before the basket was persisted in the database.
     */
    void mergeSessionBasket(HttpSession session, String clientEmail);
}
