package com.spring.project.controller;

import com.spring.project.dto.EmployeeDTO;
import com.spring.project.service.EmployeeService;
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
@RequestMapping("/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDTO> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{email}")
    public EmployeeDTO getEmployeeByEmail(@PathVariable String email) {
        return employeeService.getEmployeeByEmail(email);
    }

    @PostMapping
    public EmployeeDTO addEmployee(@RequestBody @Valid EmployeeDTO employee) {
        return employeeService.addEmployee(employee);
    }

    @PutMapping("/{email}")
    public EmployeeDTO updateEmployeeByEmail(@PathVariable String email, @RequestBody @Valid EmployeeDTO employee) {
        return employeeService.updateEmployeeByEmail(email, employee);
    }

    @DeleteMapping("/{email}")
    public void deleteEmployeeByEmail(@PathVariable String email) {
        employeeService.deleteEmployeeByEmail(email);
    }

    @PutMapping("/clients/{email}/block")
    public void blockClient(@PathVariable String email) {
        employeeService.blockClient(email);
    }

    @PutMapping("/clients/{email}/unblock")
    public void unblockClient(@PathVariable String email) {
        employeeService.unblockClient(email);
    }
}
