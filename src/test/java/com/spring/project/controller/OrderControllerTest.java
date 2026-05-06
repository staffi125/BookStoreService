package com.spring.project.controller;

import com.spring.project.dto.OrderDTO;
import com.spring.project.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser
    void testGetClientOrders() throws Exception {
        OrderDTO order = new OrderDTO();
        order.setId(1L);
        order.setClientEmail("test@email.com");

        when(orderService.getOrdersByClient("test@email.com")).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/client/test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientEmail").value("test@email.com"));
    }

    @Test
    @WithMockUser
    void testGetEmployeeOrders() throws Exception {
        OrderDTO order = new OrderDTO();
        order.setId(2L);
        order.setEmployeeEmail("emp@test.com");

        when(orderService.getOrdersByEmployee("emp@test.com")).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/employee/emp@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testConfirmOrder() throws Exception {
        OrderDTO confirmed = new OrderDTO();
        confirmed.setId(1L);

        when(orderService.confirmOrder(1L, "emp@test.com")).thenReturn(confirmed);

        mockMvc.perform(put("/orders/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "client@test.com", roles = "CLIENT")
    void testCancelOrder() throws Exception {
        OrderDTO cancelled = new OrderDTO();
        cancelled.setId(1L);

        when(orderService.cancelOrderByClient(1L, "client@test.com")).thenReturn(cancelled);

        mockMvc.perform(put("/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
