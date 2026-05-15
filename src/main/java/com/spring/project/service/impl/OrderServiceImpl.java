package com.spring.project.service.impl;

import com.spring.project.dto.BookItemDTO;
import com.spring.project.dto.OrderDTO;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Book;
import com.spring.project.model.BookItem;
import com.spring.project.model.Client;
import com.spring.project.model.Employee;
import com.spring.project.model.Order;
import com.spring.project.model.enums.OrderStatus;
import com.spring.project.repo.BookRepository;
import com.spring.project.repo.ClientRepository;
import com.spring.project.repo.EmployeeRepository;
import com.spring.project.repo.OrderRepository;
import com.spring.project.service.BasketService;
import com.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;
    private final BasketService basketService;
    private final ModelMapper modelMapper;
    private final ObjectProvider<OrderService> orderServiceSelf;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return orderRepository.findAllByClientEmail(clientEmail).stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return orderRepository.findAllByEmployeeEmail(employeeEmail).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .sorted(Comparator.comparing(OrderDTO::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO addOrder(OrderDTO order) {
        Client client = clientRepository.findByEmail(order.getClientEmail())
                .orElseThrow(() -> new NotFoundException("Client not found: " + order.getClientEmail()));
        if (client.isBlocked()) {
            throw new AccessDeniedException("Blocked clients cannot create orders");
        }
        BigDecimal total = order.getPrice();
        if (client.getBalance().compareTo(total) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        client.setBalance(client.getBalance().subtract(total));
        clientRepository.save(client);

        Employee employee = null;
        if (order.getEmployeeEmail() != null && !order.getEmployeeEmail().isBlank()) {
            employee = employeeRepository.findByEmail(order.getEmployeeEmail())
                    .orElseThrow(() -> new NotFoundException("Employee not found: " + order.getEmployeeEmail()));
        }

        Order orderToSave = new Order();
        orderToSave.setClient(client);
        orderToSave.setEmployee(employee);
        orderToSave.setOrderDate(order.getOrderDate());
        orderToSave.setPrice(order.getPrice());
        orderToSave.setStatus(OrderStatus.NEW);

        List<BookItem> bookItems = order.getBookItems().stream().map(itemDto -> {
            Book book = bookRepository.findByName(itemDto.getBookName())
                    .orElseThrow(() -> new NotFoundException("Book not found: " + itemDto.getBookName()));
            BookItem item = new BookItem();
            item.setBook(book);
            item.setQuantity(itemDto.getQuantity());
            item.setOrder(orderToSave);
            return item;
        }).toList();
        orderToSave.setBookItems(bookItems);

        Order saved = orderRepository.save(orderToSave);
        log.info("Order id={} created client={} total={} items={}",
                saved.getId(), order.getClientEmail(), order.getPrice(), bookItems.size());
        return modelMapper.map(saved, OrderDTO.class);
    }

    @Override
    @Transactional
    public OrderDTO confirmOrder(Long id, String employeeEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Only orders in NEW status can be confirmed");
        }
        Employee current = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeEmail));
        if (order.getEmployee() == null) {
            order.setEmployee(current);
        } else if (!order.getEmployee().getEmail().equals(employeeEmail)) {
            throw new AccessDeniedException("Another bookseller is assigned to this order");
        }
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        log.info("Order id={} confirmed by employee={}", id, employeeEmail);
        return modelMapper.map(saved, OrderDTO.class);
    }

    @Override
    @Transactional
    public OrderDTO markOrderShipped(Long id, String employeeEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        assertAssignedBookseller(order, employeeEmail);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be marked as shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        log.info("Order id={} marked SHIPPED by employee={}", id, employeeEmail);
        return modelMapper.map(saved, OrderDTO.class);
    }

    @Override
    @Transactional
    public OrderDTO markOrderDelivered(Long id, String employeeEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        assertAssignedBookseller(order, employeeEmail);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only SHIPPED orders can be marked as delivered");
        }
        order.setStatus(OrderStatus.DELIVERED);
        Order saved = orderRepository.save(order);
        log.info("Order id={} marked DELIVERED by employee={}", id, employeeEmail);
        return modelMapper.map(saved, OrderDTO.class);
    }

    private void assertAssignedBookseller(Order order, String employeeEmail) {
        employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeEmail));
        if (order.getEmployee() == null || !order.getEmployee().getEmail().equals(employeeEmail)) {
            throw new AccessDeniedException("Only the assigned bookseller can update this order");
        }
    }

    @Override
    @Transactional
    public OrderDTO cancelOrderByClient(Long id, String clientEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        if (!order.getClient().getEmail().equals(clientEmail)) {
            throw new AccessDeniedException("Not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Only orders in NEW status can be cancelled by the client");
        }
        Client client = order.getClient();
        client.setBalance(client.getBalance().add(order.getPrice()));
        clientRepository.save(client);
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order id={} cancelled by client={} refund={}", id, clientEmail, order.getPrice());
        return modelMapper.map(saved, OrderDTO.class);
    }

    @Override
    @Transactional
    public OrderDTO checkoutFromBasket(String clientEmail) {
        List<BookItemDTO> lines = basketService.getItems(clientEmail);
        if (lines.isEmpty()) {
            throw new IllegalStateException("Basket is empty");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BookItemDTO line : lines) {
            Book book = bookRepository.findByName(line.getBookName())
                    .orElseThrow(() -> new NotFoundException("Book not found: " + line.getBookName()));
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        OrderDTO order = new OrderDTO();
        order.setClientEmail(clientEmail);
        order.setEmployeeEmail(null);
        order.setOrderDate(LocalDateTime.now());
        order.setPrice(total);
        order.setBookItems(new ArrayList<>(lines));
        OrderDTO saved = orderServiceSelf.getObject().addOrder(order);
        basketService.clear(clientEmail);
        log.info("Checkout completed client={} orderId={} lines={} total={}",
                clientEmail, saved.getId(), lines.size(), total);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findUnassignedNewOrders() {
        return orderRepository.findByEmployeeIsNullAndStatusOrderByOrderDateDesc(OrderStatus.NEW).stream()
                .map(o -> modelMapper.map(o, OrderDTO.class))
                .toList();
    }
}
