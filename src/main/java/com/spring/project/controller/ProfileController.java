package com.spring.project.controller;

import com.spring.project.dto.ClientDTO;
import com.spring.project.dto.EmployeeDTO;
import com.spring.project.service.ClientService;
import com.spring.project.service.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public Object viewProfile(@AuthenticationPrincipal UserDetails principal) {
        if (principal.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()))) {
            return clientService.getClientByEmail(principal.getUsername());
        }
        if (principal.getAuthorities().stream().anyMatch(a -> "ROLE_EMPLOYEE".equals(a.getAuthority()))) {
            return employeeService.getEmployeeByEmail(principal.getUsername());
        }
        throw new IllegalStateException("Unsupported principal type");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('CLIENT','EMPLOYEE')")
    public Object editProfile(@AuthenticationPrincipal UserDetails principal, @RequestBody JsonNode body)
            throws JsonProcessingException {
        if (principal.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()))) {
            ClientDTO dto = objectMapper.treeToValue(body, ClientDTO.class);
            validate(dto);
            if (!principal.getUsername().equals(dto.getEmail())) {
                throw new AccessDeniedException("Email must match authenticated user");
            }
            return clientService.updateOwnProfile(principal.getUsername(), dto);
        }
        if (principal.getAuthorities().stream().anyMatch(a -> "ROLE_EMPLOYEE".equals(a.getAuthority()))) {
            EmployeeDTO dto = objectMapper.treeToValue(body, EmployeeDTO.class);
            validate(dto);
            if (!principal.getUsername().equals(dto.getEmail())) {
                throw new AccessDeniedException("Email must match authenticated user");
            }
            return employeeService.updateEmployeeByEmail(principal.getUsername(), dto);
        }
        throw new AccessDeniedException("Unsupported principal type");
    }

    private <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
