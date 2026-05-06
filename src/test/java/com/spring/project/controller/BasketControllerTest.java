package com.spring.project.controller;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.service.BasketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BasketController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketService basketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CLIENT")
    void testGetBasket() throws Exception {
        BookItemDTO item = new BookItemDTO("Test Book", 2);
        when(basketService.getItems(any())).thenReturn(List.of(item));

        mockMvc.perform(get("/basket").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookName").value("Test Book"))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testAddItem() throws Exception {
        BookItemDTO item = new BookItemDTO("Test Book", 1);
        when(basketService.getItems(any())).thenReturn(List.of(item));

        mockMvc.perform(post("/basket/items")
                .session(new MockHttpSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookName").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testClearBasket() throws Exception {
        doNothing().when(basketService).clear(any());

        mockMvc.perform(delete("/basket").session(new MockHttpSession()))
                .andExpect(status().isOk());
    }
}
