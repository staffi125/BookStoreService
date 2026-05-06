package com.spring.project.controller.web;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderWebController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = "CLIENT")
    void testMyOrdersRedirect() throws Exception {
        mockMvc.perform(get("/app/my-orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/profile#orders"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testCheckoutRedirect() throws Exception {
        mockMvc.perform(get("/app/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testEmployeeOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(new OrderDTO()));

        mockMvc.perform(get("/app/employee/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("currentStaffEmail", "emp@test.com"));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testConfirmOrder() throws Exception {
        when(orderService.confirmOrder(1L, "emp@test.com")).thenReturn(new OrderDTO());

        mockMvc.perform(post("/app/employee/orders/confirm")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/employee/orders"));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testConfirmOrder_WhenFails() throws Exception {
        when(orderService.confirmOrder(1L, "emp@test.com"))
                .thenThrow(new IllegalStateException("Already confirmed"));

        mockMvc.perform(post("/app/employee/orders/confirm")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/employee/orders"))
                .andExpect(flash().attribute("flashError", "orders.confirm_failed"));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testShipOrder() throws Exception {
        when(orderService.markOrderShipped(1L, "emp@test.com")).thenReturn(new OrderDTO());

        mockMvc.perform(post("/app/employee/orders/ship")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/employee/orders"));
    }

    @Test
    @WithMockUser(username = "emp@test.com", roles = "EMPLOYEE")
    void testDeliverOrder() throws Exception {
        when(orderService.markOrderDelivered(1L, "emp@test.com")).thenReturn(new OrderDTO());

        mockMvc.perform(post("/app/employee/orders/deliver")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/employee/orders"));
    }
}
