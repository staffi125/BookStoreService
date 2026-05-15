package com.spring.project.service;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.BasketEntry;
import com.spring.project.model.Book;
import com.spring.project.model.Client;
import com.spring.project.repo.BasketEntryRepository;
import com.spring.project.repo.BookRepository;
import com.spring.project.repo.ClientRepository;
import com.spring.project.service.impl.DbBasketService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DbBasketServiceTest {

    private static final String CLIENT_EMAIL = "client@example.com";

    @Mock
    private BasketEntryRepository basketEntryRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private HttpSession session;

    @InjectMocks
    private DbBasketService basketService;

    private Client client;
    private Book book;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setEmail(CLIENT_EMAIL);

        book = new Book();
        book.setId(10L);
        book.setName("Book1");
    }

    @Test
    void addItem_createsNewEntry() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(bookRepository.findByName("Book1")).thenReturn(Optional.of(book));
        when(basketEntryRepository.findByClient_EmailAndBook_Name(CLIENT_EMAIL, "Book1"))
                .thenReturn(Optional.empty());

        basketService.addItem(CLIENT_EMAIL, "Book1", 2);

        ArgumentCaptor<BasketEntry> captor = ArgumentCaptor.forClass(BasketEntry.class);
        verify(basketEntryRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getQuantity());
        assertEquals(client, captor.getValue().getClient());
        assertEquals(book, captor.getValue().getBook());
    }

    @Test
    void addItem_mergesQuantityForExistingEntry() {
        BasketEntry existing = new BasketEntry();
        existing.setQuantity(1);
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(bookRepository.findByName("Book1")).thenReturn(Optional.of(book));
        when(basketEntryRepository.findByClient_EmailAndBook_Name(CLIENT_EMAIL, "Book1"))
                .thenReturn(Optional.of(existing));

        basketService.addItem(CLIENT_EMAIL, "Book1", 3);

        assertEquals(4, existing.getQuantity());
        verify(basketEntryRepository, never()).save(any());
    }

    @Test
    void addItem_zeroQuantityIsIgnored() {
        basketService.addItem(CLIENT_EMAIL, "Book1", 0);
        verify(clientRepository, never()).findByEmail(any());
    }

    @Test
    void removeItem_deletesByClientAndBook() {
        basketService.removeItem(CLIENT_EMAIL, "Book1");
        verify(basketEntryRepository).deleteByClient_EmailAndBook_Name(CLIENT_EMAIL, "Book1");
    }

    @Test
    void clear_deletesAllForClient() {
        basketService.clear(CLIENT_EMAIL);
        verify(basketEntryRepository).deleteByClient_Email(CLIENT_EMAIL);
    }

    @Test
    void getItems_mapsEntitiesToDto() {
        BasketEntry entry = new BasketEntry();
        entry.setBook(book);
        entry.setQuantity(2);
        when(basketEntryRepository.findByClient_EmailOrderByIdAsc(CLIENT_EMAIL))
                .thenReturn(List.of(entry));

        List<BookItemDTO> items = basketService.getItems(CLIENT_EMAIL);

        assertEquals(1, items.size());
        assertEquals("Book1", items.get(0).getBookName());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    void addItem_unknownBookThrows() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(bookRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> basketService.addItem(CLIENT_EMAIL, "Missing", 1));
    }

    @Test
    void mergeSessionBasket_importsLegacySessionItems() {
        List<BookItemDTO> legacy = new ArrayList<>();
        legacy.add(new BookItemDTO("Book1", 2));
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(legacy);
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(bookRepository.findByName("Book1")).thenReturn(Optional.of(book));
        when(basketEntryRepository.findByClient_EmailAndBook_Name(CLIENT_EMAIL, "Book1"))
                .thenReturn(Optional.empty());

        basketService.mergeSessionBasket(session, CLIENT_EMAIL);

        verify(basketEntryRepository, atLeastOnce()).save(any());
        verify(session).removeAttribute("BOOK_STORE_BASKET_ITEMS");
    }

    @Test
    void mergeSessionBasket_noOpWhenSessionEmpty() {
        when(session.getAttribute("BOOK_STORE_BASKET_ITEMS")).thenReturn(null);

        basketService.mergeSessionBasket(session, CLIENT_EMAIL);

        verify(basketEntryRepository, never()).save(any());
    }
}
