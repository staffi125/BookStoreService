package com.spring.project.controller.web;

import com.spring.project.dto.OrderDTO;
import com.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class OrderWebController {

    private final OrderService orderService;

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CLIENT')")
    public String myOrdersRedirect() {
        return "redirect:/app/profile#orders";
    }

    @GetMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
    public String checkoutRedirect() {
        return "redirect:/app/basket";
    }

    @GetMapping("/employee/orders")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String employeeOrders(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("currentStaffEmail", principal.getUsername());
        return "employee/orders";
    }

    @GetMapping(value = "/employee/orders/data", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @ResponseBody
    public List<OrderDTO> employeeOrdersData() {
        return orderService.getAllOrders();
    }

    @PostMapping("/employee/orders/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String confirmOrder(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.confirmOrder(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.confirmed");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.confirm_failed");
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.confirm_denied");
        }
        return "redirect:/app/employee/orders";
    }

    @PostMapping("/employee/orders/ship")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String shipOrder(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.markOrderShipped(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.shipped");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.ship_failed");
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.ship_denied");
        }
        return "redirect:/app/employee/orders";
    }

    @PostMapping("/employee/orders/deliver")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deliverOrder(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.markOrderDelivered(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.delivered");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.deliver_failed");
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.deliver_denied");
        }
        return "redirect:/app/employee/orders";
    }
}
