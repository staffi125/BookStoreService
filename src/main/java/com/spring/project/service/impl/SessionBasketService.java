package com.spring.project.service.impl;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.service.BasketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SessionBasketService implements BasketService {

    private static final String SESSION_KEY = "BOOK_STORE_BASKET_ITEMS";

    @Override
    @SuppressWarnings("unchecked")
    public void addItem(HttpSession session, String bookName, int quantity) {
        if (quantity <= 0) {
            return;
        }
        List<BookItemDTO> items = getOrCreate(session);
        for (BookItemDTO line : items) {
            if (line.getBookName().equals(bookName)) {
                line.setQuantity(line.getQuantity() + quantity);
                session.setAttribute(SESSION_KEY, items);
                log.debug("Basket merge book={} newQty={} sessionId={}", bookName, line.getQuantity(), session.getId());
                return;
            }
        }
        items.add(new BookItemDTO(bookName, quantity));
        session.setAttribute(SESSION_KEY, items);
        log.debug("Basket add book={} qty={} sessionId={}", bookName, quantity, session.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeItem(HttpSession session, String bookName) {
        List<BookItemDTO> items = getOrCreate(session);
        items.removeIf(line -> line.getBookName().equals(bookName));
        session.setAttribute(SESSION_KEY, items);
        log.debug("Basket remove book={} sessionId={}", bookName, session.getId());
    }

    @Override
    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        log.debug("Basket cleared sessionId={}", session.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BookItemDTO> getItems(HttpSession session) {
        Object attr = session.getAttribute(SESSION_KEY);
        if (attr == null) {
            return List.of();
        }
        return List.copyOf((List<BookItemDTO>) attr);
    }

    @SuppressWarnings("unchecked")
    private List<BookItemDTO> getOrCreate(HttpSession session) {
        Object attr = session.getAttribute(SESSION_KEY);
        if (attr == null) {
            List<BookItemDTO> list = new ArrayList<>();
            session.setAttribute(SESSION_KEY, list);
            return list;
        }
        return (List<BookItemDTO>) attr;
    }
}
