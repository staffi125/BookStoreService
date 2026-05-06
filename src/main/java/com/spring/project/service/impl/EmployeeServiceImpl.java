package com.spring.project.service.impl;

import com.spring.project.dto.EmployeeDTO;
import com.spring.project.exception.AlreadyExistException;
import com.spring.project.exception.NotFoundException;
import com.spring.project.model.Client;
import com.spring.project.model.Employee;
import com.spring.project.repo.ClientRepository;
import com.spring.project.repo.EmployeeRepository;
import com.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .toList();
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + email));
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employee) {
        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + email));
        Employee updated = modelMapper.map(employee, Employee.class);
        updated.setId(existing.getId());
        updated.setPassword(resolvePassword(existing.getPassword(), employee.getPassword()));
        Employee saved = employeeRepository.save(updated);
        log.info("Employee updated email={}", email);
        return modelMapper.map(saved, EmployeeDTO.class);
    }

    @Override
    public void deleteEmployeeByEmail(String email) {
        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + email));
        employeeRepository.delete(existing);
        log.info("Employee deleted email={}", email);
    }

    @Override
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new AlreadyExistException("Employee already exists: " + employee.getEmail());
        }
        Employee entity = modelMapper.map(employee, Employee.class);
        entity.setPassword(encodePassword(employee.getPassword()));
        Employee saved = employeeRepository.save(entity);
        log.info("Employee registered email={}", saved.getEmail());
        return modelMapper.map(saved, EmployeeDTO.class);
    }

    @Override
    public void blockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        client.setBlocked(true);
        clientRepository.save(client);
        log.info("Client blocked email={}", email);
    }

    @Override
    public void unblockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));
        client.setBlocked(false);
        clientRepository.save(client);
        log.info("Client unblocked email={}", email);
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
