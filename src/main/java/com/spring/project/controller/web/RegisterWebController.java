package com.spring.project.controller.web;

import com.spring.project.dto.ClientDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/app/register")
@RequiredArgsConstructor
public class RegisterWebController {

    private final ClientService clientService;

    @GetMapping
    public String form(Model model) {
        if (!model.containsAttribute("registerForm")) {
            ClientDTO dto = new ClientDTO();
            dto.setBalance(new BigDecimal("100.00"));
            dto.setBlocked(false);
            model.addAttribute("registerForm", dto);
        }
        return "register";
    }

    @PostMapping
    public String submit(
            @Valid @ModelAttribute("registerForm") ClientDTO dto,
            BindingResult bindingResult,
            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        dto.setBlocked(false);
        try {
            clientService.addClient(dto);
        } catch (AlreadyExistException e) {
            bindingResult.reject("register.duplicate");
            return "register";
        }
        return "redirect:/login?registered";
    }
}
