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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BasketWebController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BasketWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketService basketService;

    @MockBean
    private BookService bookService;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = "CLIENT")
    void testViewBasket_EmptyBasket() throws Exception {
        when(basketService.getItems(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/app/basket").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attribute("basketEmpty", true));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testAddToBasket() throws Exception {
        doNothing().when(basketService).addItem(any(), any(), any(int.class));
        when(basketService.getItems(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/app/basket/add")
                .session(new MockHttpSession())
                .param("bookName", "Book1")
                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testClearBasket() throws Exception {
        doNothing().when(basketService).clear(any());

        mockMvc.perform(post("/app/basket/clear").session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testRemoveFromBasket() throws Exception {
        doNothing().when(basketService).removeItem(any(), any());

        mockMvc.perform(post("/app/basket/remove")
                .session(new MockHttpSession())
                .param("bookName", "Book1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/basket"));
    }
}
