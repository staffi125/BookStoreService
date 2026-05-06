package com.spring.project.security;

import com.spring.project.model.Client;
import com.spring.project.model.Employee;
import com.spring.project.repo.ClientRepository;
import com.spring.project.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return employeeRepository.findByEmail(email)
                .map(this::toEmployeeUser)
                .or(() -> clientRepository.findByEmail(email).map(this::toClientUser))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private UserDetails toEmployeeUser(Employee employee) {
        log.debug("Loaded credentials for employee email={}", employee.getEmail());
        return User.withUsername(employee.getEmail())
                .password(employee.getPassword())
                .roles("EMPLOYEE")
                .build();
    }

    private UserDetails toClientUser(Client client) {
        log.debug("Loaded credentials for client email={} blocked={}", client.getEmail(), client.isBlocked());
        return User.withUsername(client.getEmail())
                .password(client.getPassword())
                .roles("CLIENT")
                .disabled(client.isBlocked())
                .build();
    }
}
