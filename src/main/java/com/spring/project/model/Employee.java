package com.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee extends User {
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "phone")
    private String phone;

    public Employee(Long id, String email, String password, String name, LocalDate birthDate, String phone) {
        super(id, email, password, name);
        this.birthDate = birthDate;
        this.phone = phone;
    }
}
