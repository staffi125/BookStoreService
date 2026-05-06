package com.spring.project.controller;

import com.spring.project.dto.ClientDTO;
import com.spring.project.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetAllClients() throws Exception {
        ClientDTO c = new ClientDTO();
        c.setEmail("a@test.com");
        c.setName("Alice");
        c.setBalance(BigDecimal.TEN);

        when(clientService.getAllClients()).thenReturn(List.of(c));

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@test.com"));
    }

    @Test
    @WithMockUser(username = "a@test.com", roles = "CLIENT")
    void testGetClientByEmail() throws Exception {
        ClientDTO c = new ClientDTO();
        c.setEmail("a@test.com");
        c.setName("Alice");
        c.setBalance(BigDecimal.TEN);

        when(clientService.getClientByEmail("a@test.com")).thenReturn(c);

        mockMvc.perform(get("/clients/a@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void testAddClient() throws Exception {
        ClientDTO c = new ClientDTO();
        c.setEmail("new@test.com");
        c.setPassword("StrongPass1@");
        c.setName("New Client");
        c.setBalance(new BigDecimal("100.00"));

        when(clientService.addClient(any(ClientDTO.class))).thenReturn(c);

        mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    @WithMockUser(username = "a@test.com", roles = "CLIENT")
    void testDeleteClientByEmail() throws Exception {
        doNothing().when(clientService).deleteClientByEmail("a@test.com");

        mockMvc.perform(delete("/clients/a@test.com"))
                .andExpect(status().isOk());
    }
}
