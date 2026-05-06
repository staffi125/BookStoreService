package com.spring.project.service;

import com.spring.project.dto.BookItemDTO;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface BasketService {

    void addItem(HttpSession session, String bookName, int quantity);

    void removeItem(HttpSession session, String bookName);

    void clear(HttpSession session);

    List<BookItemDTO> getItems(HttpSession session);
}
