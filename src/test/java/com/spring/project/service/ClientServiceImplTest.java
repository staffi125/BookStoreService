package com.spring.project.service;

import com.spring.project.dto.ClientDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Client;
import com.spring.project.repo.ClientRepository;
import com.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void testAddClient_Success() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("test@email.com");
        dto.setPassword("rawPassword");

        Client entity = new Client();
        entity.setEmail("test@email.com");

        when(clientRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        when(modelMapper.map(dto, Client.class)).thenReturn(entity);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(entity);
        when(modelMapper.map(entity, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.addClient(dto);

        assertNotNull(result);
        assertEquals("test@email.com", result.getEmail());
        verify(clientRepository).save(argThat(c -> c.getPassword().equals("encodedPassword") && !c.isBlocked()));
    }

    @Test
    void testAddClient_AlreadyExists() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("exist@email.com");

        when(clientRepository.findByEmail("exist@email.com")).thenReturn(Optional.of(new Client()));

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
    }

    @Test
    void testUpdateOwnProfile_Success() {
        Client existing = new Client();
        existing.setEmail("me@email.com");
        existing.setPassword("oldEncoded");

        ClientDTO dto = new ClientDTO();
        dto.setEmail("me@email.com");
        dto.setName("New Name");
        dto.setPassword("newPass");

        when(clientRepository.findByEmail("me@email.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(clientRepository.save(any(Client.class))).thenReturn(existing);
        when(modelMapper.map(existing, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.updateOwnProfile("me@email.com", dto);

        assertNotNull(result);
        verify(clientRepository).save(argThat(c -> "newEncoded".equals(c.getPassword()) && "New Name".equals(c.getName())));
    }

    @Test
    void testUpdateOwnProfile_AccessDenied() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("hacker@email.com");

        assertThrows(AccessDeniedException.class, () -> clientService.updateOwnProfile("me@email.com", dto));
    }

    @Test
    void testUpdateOwnProfile_EmptyPasswordKeepsOld() {
        Client existing = new Client();
        existing.setEmail("me@email.com");
        existing.setPassword("oldEncoded");

        ClientDTO dto = new ClientDTO();
        dto.setEmail("me@email.com");
        dto.setPassword("");

        when(clientRepository.findByEmail("me@email.com")).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(Client.class))).thenReturn(existing);
        
        clientService.updateOwnProfile("me@email.com", dto);

        verify(clientRepository).save(argThat(c -> "oldEncoded".equals(c.getPassword())));
    }
}
