package com.spring.project.service.impl;

import com.spring.project.dto.ClientDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Client;
import com.spring.project.repo.ClientRepository;
import com.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .toList();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        Client updated = modelMapper.map(client, Client.class);
        updated.setId(existing.getId());
        updated.setPassword(resolvePassword(existing.getPassword(), client.getPassword()));
        Client saved = clientRepository.save(updated);
        log.info("Client updated email={}", email);
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Override
    public void deleteClientByEmail(String email) {
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        clientRepository.delete(existing);
        log.info("Client deleted email={}", email);
    }

    @Override
    public ClientDTO addClient(ClientDTO client) {
        if (clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new AlreadyExistException("Client already exists: " + client.getEmail());
        }
        Client entity = modelMapper.map(client, Client.class);
        entity.setPassword(encodePassword(client.getPassword()));
        entity.setBlocked(false);
        Client saved = clientRepository.save(entity);
        log.info("Client registered email={}", saved.getEmail());
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Override
    public ClientDTO updateOwnProfile(String email, ClientDTO client) {
        if (!email.equals(client.getEmail())) {
            throw new AccessDeniedException("Cannot change email");
        }
        Client existing = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        existing.setName(client.getName());
        existing.setPassword(resolvePassword(existing.getPassword(), client.getPassword()));
        existing.setBalance(existing.getBalance());
        existing.setBlocked(existing.isBlocked());
        Client saved = clientRepository.save(existing);
        log.info("Client profile updated email={}", email);
        return modelMapper.map(saved, ClientDTO.class);
    }

    private String resolvePassword(String currentEncoded, String incoming) {
        if (incoming == null || incoming.isBlank()) {
            return currentEncoded;
        }
        return encodePassword(incoming);
    }

    private String encodePassword(String rawOrEncoded) {
        if (rawOrEncoded == null || rawOrEncoded.isBlank()) {
            return rawOrEncoded;
        }
        if (rawOrEncoded.startsWith("$2a$") || rawOrEncoded.startsWith("$2b$") || rawOrEncoded.startsWith("$2y$")) {
            return rawOrEncoded;
        }
        return passwordEncoder.encode(rawOrEncoded);
    }
}
