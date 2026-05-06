package com.spring.project.service;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.dto.OrderDTO;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Book;
import com.spring.project.model.Client;
import com.spring.project.model.Employee;
import com.spring.project.model.Order;
import com.spring.project.model.enums.OrderStatus;
import com.spring.project.repo.BookRepository;
import com.spring.project.repo.ClientRepository;
import com.spring.project.repo.EmployeeRepository;
import com.spring.project.repo.OrderRepository;
import com.spring.project.service.impl.OrderServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BasketService basketService;
    @Mock private ModelMapper modelMapper;
    @Mock private ObjectProvider<OrderService> orderServiceSelf;
    @Mock private HttpSession session;
    
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testAddOrder_NotEnoughBalance() {
        Client client = new Client();
        client.setEmail("c@email.com");
        client.setBalance(BigDecimal.valueOf(10));
        
        OrderDTO order = new OrderDTO();
        order.setClientEmail("c@email.com");
        order.setPrice(BigDecimal.valueOf(100)); // Price > Balance
        
        when(clientRepository.findByEmail("c@email.com")).thenReturn(Optional.of(client));
        
        assertThrows(IllegalStateException.class, () -> orderService.addOrder(order));
    }

    @Test
    void testAddOrder_Success() {
        Client client = new Client();
        client.setEmail("c@email.com");
        client.setBalance(BigDecimal.valueOf(200));
        
        OrderDTO order = new OrderDTO();
        order.setClientEmail("c@email.com");
        order.setPrice(BigDecimal.valueOf(100));
        
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookName("Book1");
        itemDto.setQuantity(1);
        order.setBookItems(List.of(itemDto));
        
        Book book = new Book();
        book.setName("Book1");
        book.setPrice(BigDecimal.valueOf(100));

        when(clientRepository.findByEmail("c@email.com")).thenReturn(Optional.of(client));
        when(bookRepository.findByName("Book1")).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(modelMapper.map(any(), eq(OrderDTO.class))).thenReturn(order);
        
        OrderDTO saved = orderService.addOrder(order);
        
        assertNotNull(saved);
        assertEquals(BigDecimal.valueOf(100), client.getBalance()); // Balance checked
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.NEW));
    }

    @Test
    void testConfirmOrder_WrongStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        assertThrows(IllegalStateException.class, () -> orderService.confirmOrder(1L, "emp@email.com"));
    }

    @Test
    void testConfirmOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NEW);
        
        Employee emp = new Employee();
        emp.setEmail("emp@email.com");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("emp@email.com")).thenReturn(Optional.of(emp));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(modelMapper.map(any(), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        orderService.confirmOrder(1L, "emp@email.com");
        
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals("emp@email.com", order.getEmployee().getEmail());
    }

    @Test
    void testCancelOrderByClient_Success() {
        Client client = new Client();
        client.setEmail("c@email.com");
        client.setBalance(BigDecimal.valueOf(100));

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setPrice(BigDecimal.valueOf(50));
        order.setClient(client);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(modelMapper.map(any(), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        orderService.cancelOrderByClient(1L, "c@email.com");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(BigDecimal.valueOf(150), client.getBalance()); // Balance refunded
    }

    @Test
    void testCheckoutFromBasket() {
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookName("Book1");
        itemDto.setQuantity(2);
        
        Book book = new Book();
        book.setName("Book1");
        book.setPrice(BigDecimal.valueOf(50));

        when(basketService.getItems(session)).thenReturn(List.of(itemDto));
        when(bookRepository.findByName("Book1")).thenReturn(Optional.of(book));
        
        // Use spy to bypass actual addOrder execution which relies on actual client
        OrderServiceImpl spyService = spy(orderService);
        when(orderServiceSelf.getObject()).thenReturn(spyService);
        doReturn(new OrderDTO()).when(spyService).addOrder(any(OrderDTO.class));
        
        spyService.checkoutFromBasket(session, "c@email.com");
        
        verify(basketService).clear(session);
        verify(spyService).addOrder(argThat(o -> o.getPrice().compareTo(BigDecimal.valueOf(100)) == 0));
    }
}
