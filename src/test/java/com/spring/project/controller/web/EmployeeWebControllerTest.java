package com.spring.project.controller.web;

import com.spring.project.dto.BookDTO;
import com.spring.project.dto.ClientDTO;
import com.spring.project.service.BookService;
import com.spring.project.service.ClientService;
import com.spring.project.service.EmployeeService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeWebController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testDashboard() throws Exception {
        mockMvc.perform(get("/app/employee"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/dashboard"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testBooksList() throws Exception {
        BookDTO dto = new BookDTO();
        dto.setName("Test Book");
        dto.setPrice(BigDecimal.TEN);

        when(bookService.searchBooks(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/app/employee/books?page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/books"))
                .andExpect(model().attributeExists("bookPage"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testNewBookForm() throws Exception {
        mockMvc.perform(get("/app/employee/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/book-form"))
                .andExpect(model().attributeExists("bookForm"))
                .andExpect(model().attribute("editMode", false));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testClientsList() throws Exception {
        ClientDTO c = new ClientDTO();
        c.setEmail("client@test.com");
        c.setName("Client");
        c.setBalance(BigDecimal.TEN);

        when(clientService.getAllClients()).thenReturn(List.of(c));

        mockMvc.perform(get("/app/employee/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/clients"))
                .andExpect(model().attributeExists("clients"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testBlockClient() throws Exception {
        doNothing().when(employeeService).blockClient("client@test.com");

        mockMvc.perform(post("/app/employee/clients/block")
                .param("email", "client@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/employee/clients"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testDeleteBook() throws Exception {
        doNothing().when(bookService).deleteBookByName("Test Book");

        mockMvc.perform(post("/app/employee/books/delete")
                .param("name", "Test Book"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/employee/books"));
    }
}
