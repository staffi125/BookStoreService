package com.spring.project.service.impl;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.BasketEntry;
import com.spring.project.model.Book;
import com.spring.project.model.Client;
import com.spring.project.repo.BasketEntryRepository;
import com.spring.project.repo.BookRepository;
import com.spring.project.repo.ClientRepository;
import com.spring.project.service.BasketService;
import com.spring.project.util.EmailNormalizer;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbBasketService implements BasketService {

    private static final String LEGACY_SESSION_KEY = "BOOK_STORE_BASKET_ITEMS";

    private final BasketEntryRepository basketEntryRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void addItem(String clientEmail, String bookName, int quantity) {
        if (quantity <= 0) {
            return;
        }
        String email = EmailNormalizer.normalize(clientEmail);
        Client client = requireClient(email);
        Book book = requireBook(bookName);

        basketEntryRepository.findByClient_EmailAndBook_Name(email, book.getName())
                .ifPresentOrElse(
                        entry -> entry.setQuantity(entry.getQuantity() + quantity),
                        () -> {
                            BasketEntry entry = new BasketEntry();
                            entry.setClient(client);
                            entry.setBook(book);
                            entry.setQuantity(quantity);
                            basketEntryRepository.save(entry);
                        });
        log.debug("Basket add client={} book={} qty={}", email, book.getName(), quantity);
    }

    @Override
    @Transactional
    public void removeItem(String clientEmail, String bookName) {
        String email = EmailNormalizer.normalize(clientEmail);
        basketEntryRepository.deleteByClient_EmailAndBook_Name(email, bookName);
        log.debug("Basket remove client={} book={}", email, bookName);
    }

    @Override
    @Transactional
    public void clear(String clientEmail) {
        String email = EmailNormalizer.normalize(clientEmail);
        basketEntryRepository.deleteByClient_Email(email);
        log.debug("Basket cleared client={}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookItemDTO> getItems(String clientEmail) {
        String email = EmailNormalizer.normalize(clientEmail);
        return basketEntryRepository.findByClient_EmailOrderByIdAsc(email).stream()
                .map(entry -> new BookItemDTO(entry.getBook().getName(), entry.getQuantity()))
                .toList();
    }

    @Override
    @Transactional
    public void mergeSessionBasket(HttpSession session, String clientEmail) {
        if (session == null) {
            return;
        }
        Object attr = session.getAttribute(LEGACY_SESSION_KEY);
        if (!(attr instanceof List<?> rawList) || rawList.isEmpty()) {
            return;
        }
        List<BookItemDTO> migrated = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof BookItemDTO dto) {
                addItem(clientEmail, dto.getBookName(), dto.getQuantity());
                migrated.add(dto);
            }
        }
        session.removeAttribute(LEGACY_SESSION_KEY);
        if (!migrated.isEmpty()) {
            log.info("Migrated {} basket line(s) from session to DB for client={}", migrated.size(), clientEmail);
        }
    }

    private Client requireClient(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
    }

    private Book requireBook(String bookName) {
        return bookRepository.findByName(bookName)
                .orElseThrow(() -> new NotFoundException("Book not found: " + bookName));
    }
}
