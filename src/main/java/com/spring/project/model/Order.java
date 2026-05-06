package com.spring.project.model;

import com.spring.project.model.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    @ManyToOne(optional = true)
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee employee;
    @Column(name = "order_date")
    private LocalDateTime orderDate;
    @Column(name = "price")
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<BookItem> bookItems;

    public Order(Long id, Client client, Employee employee, LocalDateTime orderDate, BigDecimal price, OrderStatus status,
                 List<BookItem> bookItems) {
        this.id = id;
        this.client = client;
        this.employee = employee;
        this.orderDate = orderDate;
        this.price = price;
        this.status = status;
        this.bookItems = bookItems;
    }
}
