package com.spring.project.service;

import com.spring.project.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    OrderDTO confirmOrder(Long id, String employeeEmail);

    /**
     * {@code CONFIRMED} → {@code SHIPPED}. Only the bookseller assigned to the order may call this (employee role at API/UI layer).
     */
    OrderDTO markOrderShipped(Long id, String employeeEmail);

    /**
     * {@code SHIPPED} → {@code DELIVERED}. Only the assigned bookseller.
     */
    OrderDTO markOrderDelivered(Long id, String employeeEmail);

    /**
     * Client cancels a {@link com.spring.project.model.enums.OrderStatus#NEW} order before a bookseller confirms it;
     * order total is refunded to the client's balance.
     */
    OrderDTO cancelOrderByClient(Long id, String clientEmail);

    OrderDTO checkoutFromBasket(String clientEmail);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> findUnassignedNewOrders();
}
