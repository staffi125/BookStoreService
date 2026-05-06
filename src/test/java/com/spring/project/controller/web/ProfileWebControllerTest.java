package com.spring.project.controller.web;

import com.spring.project.dto.ClientDTO;
import com.spring.project.service.ClientService;
import com.spring.project.service.EmployeeService;
import com.spring.project.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileWebController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(username = "client@email.com", roles = "CLIENT")
    void testGetProfile_Client() throws Exception {
        ClientDTO client = new ClientDTO();
        client.setEmail("client@email.com");
        client.setName("Client Name");
        
        when(clientService.getClientByEmail("client@email.com")).thenReturn(client);

        mockMvc.perform(get("/app/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("clientProfileForm"))
                .andExpect(model().attribute("profileRole", "CLIENT"));
    }

    @Test
    @WithMockUser(username = "client@email.com", roles = "CLIENT")
    void testPostProfileClient_Success() throws Exception {
        ClientDTO current = new ClientDTO();
        current.setEmail("client@email.com");
        current.setName("Client Name");
        current.setBalance(new BigDecimal("100.00"));
        current.setBlocked(false);
        when(clientService.getClientByEmail("client@email.com")).thenReturn(current);
        when(clientService.updateOwnProfile(anyString(), any())).thenReturn(current);

        mockMvc.perform(post("/app/profile/client")
                .param("name", "New Name")
                .param("password", "")) // empty password is valid
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/profile"));
    }

    @Test
    @WithMockUser(username = "client@email.com", roles = "CLIENT")
    void testPostProfileClient_InvalidPasswordPolicyExt() throws Exception {
        ClientDTO current = new ClientDTO();
        current.setEmail("client@email.com");
        current.setName("Client Name");
        current.setBalance(new BigDecimal("100.00"));
        current.setBlocked(false);
        when(clientService.getClientByEmail("client@email.com")).thenReturn(current);

        // Here we test what the user specifically asked for: validation error when password is provided but invalid
        mockMvc.perform(post("/app/profile/client")
                .param("name", "New Name")
                .param("password", "weakpass")) // weak password fails @ValidPassword
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("clientProfileForm", "password"));
    }
}
