package com.spring.project.service;

import com.spring.project.dto.EmployeeDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Client;
import com.spring.project.model.Employee;
import com.spring.project.repo.ClientRepository;
import com.spring.project.repo.EmployeeRepository;
import com.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void testAddEmployee_Success() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("emp@email.com");
        dto.setPassword("rawPass");

        Employee entity = new Employee();
        entity.setEmail("emp@email.com");

        when(employeeRepository.findByEmail("emp@email.com")).thenReturn(Optional.empty());
        when(modelMapper.map(dto, Employee.class)).thenReturn(entity);
        when(passwordEncoder.encode("rawPass")).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class))).thenReturn(entity);
        when(modelMapper.map(entity, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.addEmployee(dto);

        assertNotNull(result);
        verify(employeeRepository).save(argThat(e -> "encoded".equals(e.getPassword())));
    }

    @Test
    void testAddEmployee_AlreadyExists() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("exist@email.com");

        when(employeeRepository.findByEmail("exist@email.com")).thenReturn(Optional.of(new Employee()));

        assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));
    }

    @Test
    void testBlockClient_Success() {
        Client client = new Client();
        client.setEmail("client@email.com");
        client.setBlocked(false);

        when(clientRepository.findByEmail("client@email.com")).thenReturn(Optional.of(client));
        
        employeeService.blockClient("client@email.com");

        assertTrue(client.isBlocked());
        verify(clientRepository).save(client);
    }
    
    @Test
    void testUnblockClient_Success() {
        Client client = new Client();
        client.setEmail("client@email.com");
        client.setBlocked(true);

        when(clientRepository.findByEmail("client@email.com")).thenReturn(Optional.of(client));
        
        employeeService.unblockClient("client@email.com");

        assertFalse(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void testUpdateEmployeeByEmail_Success() {
        Employee existing = new Employee();
        existing.setId(1L);
        existing.setEmail("old@email.com");
        existing.setPassword("oldEncoded");

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@email.com");
        dto.setPassword("newPass");

        Employee updated = new Employee();
        updated.setEmail("new@email.com");

        when(employeeRepository.findByEmail("old@email.com")).thenReturn(Optional.of(existing));
        when(modelMapper.map(dto, Employee.class)).thenReturn(updated);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);
        when(modelMapper.map(updated, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.updateEmployeeByEmail("old@email.com", dto);

        assertNotNull(result);
        verify(employeeRepository).save(argThat(e -> "newEncoded".equals(e.getPassword()) && e.getId() == 1L));
    }
}
