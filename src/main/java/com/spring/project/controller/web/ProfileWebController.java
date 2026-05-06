package com.spring.project.controller.web;

import com.spring.project.dto.ClientDTO;
import com.spring.project.dto.ClientProfileForm;
import com.spring.project.dto.EmployeeDTO;
import com.spring.project.dto.EmployeeProfileForm;
import com.spring.project.exception.NotFoundException;
import com.spring.project.service.ClientService;
import com.spring.project.service.EmployeeService;
import com.spring.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/profile")
@RequiredArgsConstructor
public class ProfileWebController {

    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public String view(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (isClient(principal)) {
            populateClientModel(principal.getUsername(), model);
        } else {
            populateEmployeeModel(principal.getUsername(), model);
        }
        return "profile";
    }

    @PostMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public String updateClient(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @ModelAttribute("clientProfileForm") ClientProfileForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            populateClientModel(principal.getUsername(), model);
            return "profile";
        }
        ClientDTO current = clientService.getClientByEmail(principal.getUsername());
        ClientDTO dto = new ClientDTO();
        dto.setEmail(principal.getUsername());
        dto.setName(form.getName());
        dto.setPassword(form.getPassword() == null ? "" : form.getPassword());
        dto.setBalance(current.getBalance());
        dto.setBlocked(current.isBlocked());
        clientService.updateOwnProfile(principal.getUsername(), dto);
        ra.addFlashAttribute("flashSuccess", "profile.saved");
        return "redirect:/app/profile";
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public String getClientRedirect() {
        return "redirect:/app/profile";
    }

    @PostMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String updateEmployee(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @ModelAttribute("employeeProfileForm") EmployeeProfileForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            populateEmployeeModel(principal.getUsername(), model);
            return "profile";
        }
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail(principal.getUsername());
        dto.setName(form.getName());
        dto.setBirthDate(form.getBirthDate());
        dto.setPhone(form.getPhone());
        dto.setPassword(form.getPassword() == null ? "" : form.getPassword());
        employeeService.updateEmployeeByEmail(principal.getUsername(), dto);
        ra.addFlashAttribute("flashSuccess", "profile.saved");
        return "redirect:/app/profile";
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getEmployeeRedirect() {
        return "redirect:/app/profile";
    }

    @PostMapping("/employee/orders/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String confirmFromProfile(
            @AuthenticationPrincipal UserDetails principal,
            @org.springframework.web.bind.annotation.RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.confirmOrder(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.confirmed");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.confirm_failed");
        } catch (org.springframework.security.access.AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.confirm_denied");
        }
        return "redirect:/app/profile#staff-orders";
    }

    @PostMapping("/employee/orders/ship")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String shipFromProfile(
            @AuthenticationPrincipal UserDetails principal,
            @org.springframework.web.bind.annotation.RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.markOrderShipped(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.shipped");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.ship_failed");
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.ship_denied");
        } catch (NotFoundException e) {
            ra.addFlashAttribute("flashError", "error.not_found");
        }
        return "redirect:/app/profile#staff-orders";
    }

    @PostMapping("/employee/orders/deliver")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deliverFromProfile(
            @AuthenticationPrincipal UserDetails principal,
            @org.springframework.web.bind.annotation.RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.markOrderDelivered(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.delivered");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.deliver_failed");
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.deliver_denied");
        } catch (NotFoundException e) {
            ra.addFlashAttribute("flashError", "error.not_found");
        }
        return "redirect:/app/profile#staff-orders";
    }

    @PostMapping("/client/orders/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public String cancelOrderFromProfile(
            @AuthenticationPrincipal UserDetails principal,
            @org.springframework.web.bind.annotation.RequestParam Long id,
            RedirectAttributes ra) {
        try {
            orderService.cancelOrderByClient(id, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "orders.cancelled");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("flashError", "orders.cancel_failed");
        } catch (org.springframework.security.access.AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "orders.cancel_denied");
        } catch (NotFoundException e) {
            ra.addFlashAttribute("flashError", "error.not_found");
        }
        return "redirect:/app/profile#orders";
    }

    private void populateClientModel(String email, Model model) {
        ClientDTO current = clientService.getClientByEmail(email);
        if (!model.containsAttribute("clientProfileForm")) {
            ClientProfileForm form = new ClientProfileForm();
            form.setName(current.getName());
            model.addAttribute("clientProfileForm", form);
        }
        model.addAttribute("clientEmail", current.getEmail());
        model.addAttribute("clientBalance", current.getBalance());
        model.addAttribute("clientBlocked", current.isBlocked());
        model.addAttribute("clientOrders", orderService.getOrdersByClient(email));
        model.addAttribute("profileRole", "CLIENT");
    }

    private void populateEmployeeModel(String email, Model model) {
        EmployeeDTO current = employeeService.getEmployeeByEmail(email);
        if (!model.containsAttribute("employeeProfileForm")) {
            EmployeeProfileForm form = new EmployeeProfileForm();
            form.setName(current.getName());
            form.setBirthDate(current.getBirthDate());
            form.setPhone(current.getPhone());
            model.addAttribute("employeeProfileForm", form);
        }
        model.addAttribute("employeeEmail", current.getEmail());
        model.addAttribute("currentStaffEmail", email);
        model.addAttribute("unassignedOrders", orderService.findUnassignedNewOrders());
        model.addAttribute("staffOrders", orderService.getOrdersByEmployee(email));
        model.addAttribute("profileRole", "EMPLOYEE");
    }

    private static boolean isClient(UserDetails principal) {
        return principal.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
    }
}
