package com.spring.project.controller.web;

import com.spring.project.service.BasketService;
import com.spring.project.service.BookService;
import com.spring.project.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BasketWebController.class)
@AutoConfigureMockMvc(addFilters = false)
class BasketWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketService basketService;

    @MockBean
    private BookService bookService;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    void testViewBasket_EmptyBasket() throws Exception {
        doNothing().when(basketService).mergeSessionBasket(any(), eq("client@example.com"));
        when(basketService.getItems("client@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/app/basket").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attribute("basketEmpty", true));
    }

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    void testAddToBasket() throws Exception {
        doNothing().when(basketService).addItem(eq("client@example.com"), eq("Book1"), anyInt());

        mockMvc.perform(post("/app/basket/add")
                        .param("bookName", "Book1")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    void testClearBasket() throws Exception {
        doNothing().when(basketService).clear("client@example.com");

        mockMvc.perform(post("/app/basket/clear"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    void testRemoveFromBasket() throws Exception {
        doNothing().when(basketService).removeItem("client@example.com", "Book1");

        mockMvc.perform(post("/app/basket/remove")
                        .param("bookName", "Book1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }
}
