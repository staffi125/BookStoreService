package com.spring.project.controller.web;

import com.spring.project.dto.BookDTO;
import com.spring.project.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookWebController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    @WithMockUser(roles = "CLIENT")
    void testListBooks() throws Exception {
        BookDTO dto = new BookDTO();
        dto.setName("Test Book");
        dto.setPrice(BigDecimal.TEN);

        when(bookService.searchBooks(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/app/books?page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("bookPage"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testBookDetail() throws Exception {
        BookDTO dto = new BookDTO();
        dto.setName("TestBook");
        dto.setPrice(BigDecimal.TEN);

        when(bookService.getBookByName("TestBook")).thenReturn(dto);

        mockMvc.perform(get("/app/books/detail?name=TestBook"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/detail"))
                .andExpect(model().attributeExists("book"));
    }
}
