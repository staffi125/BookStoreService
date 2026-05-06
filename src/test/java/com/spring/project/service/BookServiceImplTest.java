package com.spring.project.service;

import com.spring.project.dto.BookDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Book;
import com.spring.project.repo.BookRepository;
import com.spring.project.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void testGetBookByName_Success() {
        Book book = new Book();
        book.setName("TestBook");
        when(bookRepository.findByName("TestBook")).thenReturn(Optional.of(book));

        BookDTO bookDTO = new BookDTO();
        bookDTO.setName("TestBook");
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookByName("TestBook");
        assertEquals("TestBook", result.getName());
    }

    @Test
    void testGetBookByName_NotFound() {
        when(bookRepository.findByName("Unknown")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookService.getBookByName("Unknown"));
    }

    @Test
    void testAddBook_Success() {
        BookDTO dto = new BookDTO();
        dto.setName("NewBook");

        Book book = new Book();
        book.setName("NewBook");

        when(bookRepository.findByName("NewBook")).thenReturn(Optional.empty());
        when(modelMapper.map(dto, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(dto);

        BookDTO result = bookService.addBook(dto);
        assertNotNull(result);
        assertEquals("NewBook", result.getName());
    }

    @Test
    void testAddBook_AlreadyExists() {
        BookDTO dto = new BookDTO();
        dto.setName("NewBook");

        when(bookRepository.findByName("NewBook")).thenReturn(Optional.of(new Book()));

        assertThrows(AlreadyExistException.class, () -> bookService.addBook(dto));
    }

    @Test
    void testDeleteBookByName_Success() {
        Book book = new Book();
        book.setName("DeleteMe");
        when(bookRepository.findByName("DeleteMe")).thenReturn(Optional.of(book));

        bookService.deleteBookByName("DeleteMe");
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void testUpdateBookByName_Success() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setName("OldName");

        BookDTO dto = new BookDTO();
        dto.setName("NewName");

        Book updated = new Book();
        updated.setName("NewName");

        when(bookRepository.findByName("OldName")).thenReturn(Optional.of(existing));
        when(modelMapper.map(dto, Book.class)).thenReturn(updated);
        when(bookRepository.save(updated)).thenReturn(updated);
        when(modelMapper.map(updated, BookDTO.class)).thenReturn(dto);

        BookDTO result = bookService.updateBookByName("OldName", dto);
        assertEquals("NewName", result.getName());
        assertEquals(1L, updated.getId());
    }

    @Test
    void testSearchBooks() {
        Book book = new Book();
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        
        BookDTO dto = new BookDTO();
        when(modelMapper.map(book, BookDTO.class)).thenReturn(dto);

        Page<BookDTO> result = bookService.searchBooks("query", BigDecimal.ONE, BigDecimal.TEN, 10, 100, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
