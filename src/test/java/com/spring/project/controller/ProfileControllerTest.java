package com.spring.project.controller;

import com.spring.project.dto.ClientDTO;
import com.spring.project.dto.EmployeeDTO;
import com.spring.project.service.ClientService;
import com.spring.project.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(username = "client@test.com", roles = "CLIENT")
    void testViewProfile_Client() throws Exception {
        ClientDTO c = new ClientDTO();
        c.setEmail("client@test.com");
        c.setName("Client");
        c.setBalance(BigDecimal.valueOf(200));

        when(clientService.getClientByEmail("client@test.com")).thenReturn(c);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client@test.com"))
                .andExpect(jsonPath("$.name").value("Client"));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testViewProfile_Employee() throws Exception {
        EmployeeDTO e = new EmployeeDTO();
        e.setEmail("emp@test.com");
        e.setName("Employee");
        e.setBirthDate(LocalDate.of(1990, 5, 15));
        e.setPhone("+380991234567");

        when(employeeService.getEmployeeByEmail("emp@test.com")).thenReturn(e);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("emp@test.com"))
                .andExpect(jsonPath("$.name").value("Employee"));
    }
}
