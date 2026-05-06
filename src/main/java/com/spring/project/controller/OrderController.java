package com.spring.project.controller;

import com.spring.project.dto.OrderDTO;
import com.spring.project.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/client/{email}")
    public List<OrderDTO> getByClient(@PathVariable("email") String clientEmail) {
        return orderService.getOrdersByClient(clientEmail);
    }

    @GetMapping("/employee/{email}")
    public List<OrderDTO> getByEmployee(@PathVariable("email") String employeeEmail) {
        return orderService.getOrdersByEmployee(employeeEmail);
    }

    @PostMapping
    public OrderDTO addOrder(@RequestBody @Valid OrderDTO order) {
        return orderService.addOrder(order);
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
    public OrderDTO checkout(
            @AuthenticationPrincipal UserDetails principal,
            HttpSession session) {
        return orderService.checkoutFromBasket(session, principal.getUsername());
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public OrderDTO confirmOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return orderService.confirmOrder(id, principal.getUsername());
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public OrderDTO shipOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return orderService.markOrderShipped(id, principal.getUsername());
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public OrderDTO deliverOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return orderService.markOrderDelivered(id, principal.getUsername());
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public OrderDTO cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return orderService.cancelOrderByClient(id, principal.getUsername());
    }
}
