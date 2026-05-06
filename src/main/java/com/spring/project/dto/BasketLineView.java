package com.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BasketLineView {
    private String bookName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
