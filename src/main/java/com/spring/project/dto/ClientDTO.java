package com.spring.project.dto;

import com.spring.project.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    @NotBlank
    private String email;
    @NotBlank
    @ValidPassword
    @ToString.Exclude
    private String password;
    @NotBlank
    private String name;
    @NotNull
    @Positive
    private BigDecimal balance;
    private boolean blocked;
}
