package com.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "clients")
public class Client extends User {
    @Column(name = "balance")
    private BigDecimal balance;
    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    public Client(Long id, String email, String password, String name, BigDecimal balance, boolean blocked) {
        super(id, email, password, name);
        this.balance = balance;
        this.blocked = blocked;
    }
}
