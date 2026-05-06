package com.spring.project.dto;
import com.spring.project.model.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    @NotBlank
    private String clientEmail;
    private String employeeEmail;
    @NotNull
    private LocalDateTime orderDate;
    @NotNull
    @Positive
    private BigDecimal price;
    @NotNull
    private List<BookItemDTO> bookItems;
    private OrderStatus status;
}
