package com.spring.project.repo;

import com.spring.project.model.Order;
import com.spring.project.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByClientEmail(String clientEmail);

    List<Order> findAllByEmployeeEmail(String employeeEmail);

    List<Order> findByEmployeeIsNullAndStatusOrderByOrderDateDesc(OrderStatus status);
}

