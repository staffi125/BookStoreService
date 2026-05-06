package com.spring.project.controller.web;

import com.spring.project.dto.ClientDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterWebController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RegisterWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    void testGetRegisterPage() throws Exception {
        mockMvc.perform(get("/app/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @Test
    void testPostRegister_Success() throws Exception {
        when(clientService.addClient(any(ClientDTO.class))).thenReturn(new ClientDTO());

        mockMvc.perform(post("/app/register")
                .param("email", "test@email.com")
                .param("name", "Test Name")
                .param("password", "TestPass1@")
                .param("balance", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void testPostRegister_AlreadyExists() throws Exception {
        when(clientService.addClient(any(ClientDTO.class))).thenThrow(new AlreadyExistException("Exists"));

        mockMvc.perform(post("/app/register")
                .param("email", "exist@email.com")
                .param("name", "Test Name")
                .param("password", "TestPass1@")
                .param("balance", "100.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors()); // Global error created by the controller
    }

    @Test
    void testPostRegister_ValidationFail() throws Exception {
        // Missing name and password doesn't adhere to policy (too short "abc")
        mockMvc.perform(post("/app/register")
                .param("email", "test@email.com")
                .param("name", "") // blank
                .param("password", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "name", "password"));
    }
}
