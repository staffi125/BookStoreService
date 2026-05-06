package com.spring.project.service;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.service.impl.SessionBasketService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SessionBasketServiceTest {

    private SessionBasketService basketService;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        basketService = new SessionBasketService();
        session = mock(HttpSession.class);
    }

    @Test
    void testAddItem_NewItem() {
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(null);

        basketService.addItem(session, "Book1", 2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BookItemDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(session, atLeastOnce()).setAttribute(eq("BOOK_STORE_BASKET_ITEMS"), captor.capture());

        List<BookItemDTO> list = captor.getValue();
        assertEquals(1, list.size());
        assertEquals("Book1", list.get(0).getBookName());
        assertEquals(2, list.get(0).getQuantity());
    }

    @Test
    void testAddItem_ExistingItem() {
        List<BookItemDTO> items = new ArrayList<>();
        items.add(new BookItemDTO("Book1", 1));
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(items);

        basketService.addItem(session, "Book1", 3);

        assertEquals(1, items.size());
        assertEquals(4, items.get(0).getQuantity());
    }

    @Test
    void testAddItem_ZeroQuantity() {
        basketService.addItem(session, "Book1", 0);
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void testRemoveItem() {
        List<BookItemDTO> items = new ArrayList<>();
        items.add(new BookItemDTO("Book1", 1));
        items.add(new BookItemDTO("Book2", 2));
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(items);

        basketService.removeItem(session, "Book1");

        assertEquals(1, items.size());
        assertEquals("Book2", items.get(0).getBookName());
    }

    @Test
    void testClear() {
        basketService.clear(session);
        verify(session).removeAttribute("BOOK_STORE_BASKET_ITEMS");
    }

    @Test
    void testGetItems() {
        List<BookItemDTO> items = new ArrayList<>();
        items.add(new BookItemDTO("Book1", 1));
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(items);

        List<BookItemDTO> result = basketService.getItems(session);
        assertEquals(1, result.size());
    }

    @Test
    void testGetItems_Empty() {
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(null);
        List<BookItemDTO> result = basketService.getItems(session);
        assertTrue(result.isEmpty());
    }
}
