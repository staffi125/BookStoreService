package com.spring.project.controller;

import com.spring.project.dto.EmployeeDTO;
import com.spring.project.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetAllEmployees() throws Exception {
        EmployeeDTO e = new EmployeeDTO();
        e.setEmail("emp@test.com");
        e.setName("Employee");
        e.setBirthDate(LocalDate.of(1990, 1, 1));
        e.setPhone("+380991234567");

        when(employeeService.getAllEmployees()).thenReturn(List.of(e));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("emp@test.com"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployeeByEmail() throws Exception {
        EmployeeDTO e = new EmployeeDTO();
        e.setEmail("emp@test.com");
        e.setName("Employee");
        e.setBirthDate(LocalDate.of(1990, 1, 1));
        e.setPhone("+380991234567");

        when(employeeService.getEmployeeByEmail("emp@test.com")).thenReturn(e);

        mockMvc.perform(get("/employees/emp@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Employee"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testBlockClient() throws Exception {
        doNothing().when(employeeService).blockClient("client@test.com");

        mockMvc.perform(put("/employees/clients/client@test.com/block"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUnblockClient() throws Exception {
        doNothing().when(employeeService).unblockClient("client@test.com");

        mockMvc.perform(put("/employees/clients/client@test.com/unblock"))
                .andExpect(status().isOk());
    }
}
