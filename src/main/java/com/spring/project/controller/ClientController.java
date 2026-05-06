package com.spring.project.controller;

import com.spring.project.dto.ClientDTO;
import com.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<ClientDTO> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('EMPLOYEE') or (hasRole('CLIENT') and #email == authentication.name)")
    public ClientDTO getClientByEmail(@PathVariable String email) {
        return clientService.getClientByEmail(email);
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ClientDTO addClient(@RequestBody @Valid ClientDTO client) {
        return clientService.addClient(client);
    }

    @PutMapping("/{email}")
    @PreAuthorize("hasRole('EMPLOYEE') or (hasRole('CLIENT') and #email == authentication.name)")
    public ClientDTO updateClientByEmail(@PathVariable String email, @RequestBody @Valid ClientDTO client) {
        return clientService.updateClientByEmail(email, client);
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('EMPLOYEE') or (hasRole('CLIENT') and #email == authentication.name)")
    public void deleteClientByEmail(@PathVariable String email) {
        clientService.deleteClientByEmail(email);
    }
}
