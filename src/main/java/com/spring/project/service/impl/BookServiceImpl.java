package com.spring.project.service.impl;

import com.spring.project.dto.BookDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Book;
import com.spring.project.repo.BookRepository;
import com.spring.project.repo.BookSpecifications;
import com.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BookDTO> getAllBooks() {
        return searchBooks(null, null, null, null, null, Pageable.unpaged()).getContent();
    }

    @Override
    public Page<BookDTO> searchBooks(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minPages,
            Integer maxPages,
            Pageable pageable) {
        return bookRepository
                .findAll(BookSpecifications.catalogFilter(query, minPrice, maxPrice, minPages, maxPages), pageable)
                .map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found: " + name));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    public BookDTO updateBookByName(String name, BookDTO book) {
        Book existing = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found: " + name));
        Book updated = modelMapper.map(book, Book.class);
        updated.setId(existing.getId());
        Book saved = bookRepository.save(updated);
        log.info("Book updated name={} -> id={}", name, saved.getId());
        return modelMapper.map(saved, BookDTO.class);
    }

    @Override
    public void deleteBookByName(String name) {
        Book existing = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Book not found: " + name));
        bookRepository.delete(existing);
        log.info("Book deleted name={}", name);
    }

    @Override
    public BookDTO addBook(BookDTO book) {
        if (bookRepository.findByName(book.getName()).isPresent()) {
            throw new AlreadyExistException("Book already exists: " + book.getName());
        }
        Book saved = bookRepository.save(modelMapper.map(book, Book.class));
        log.info("Book created name={} id={}", saved.getName(), saved.getId());
        return modelMapper.map(saved, BookDTO.class);
    }
}
