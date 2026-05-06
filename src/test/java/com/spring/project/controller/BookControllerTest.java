package com.spring.project.controller;

import com.spring.project.dto.BookDTO;
import com.spring.project.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import com.spring.project.model.enums.Language;
import com.spring.project.model.enums.AgeGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass actual JWT/Session filters for isolated controller tests
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testGetBooks() throws Exception {
        BookDTO dto = new BookDTO();
        dto.setName("Test Book");
        
        when(bookService.searchBooks(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/books?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testAddBook_Valid() throws Exception {
        BookDTO dto = new BookDTO();
        dto.setName("New Book");
        dto.setAuthor("Author");
        dto.setGenre("Fiction");
        dto.setCharacteristics("Char");
        dto.setDescription("Desc");
        dto.setLanguage(Language.ENGLISH);
        dto.setAgeGroup(AgeGroup.ADULT);
        dto.setPublicationDate(LocalDate.of(2020, 1, 1));
        dto.setPages(100);
        dto.setPrice(BigDecimal.TEN);

        when(bookService.addBook(any(BookDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Book"));
    }

    @Test
    void testAddBook_InvalidPayload() throws Exception {
        // Missing required fields
        BookDTO dto = new BookDTO();
        
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Handled by @Valid
    }
}
