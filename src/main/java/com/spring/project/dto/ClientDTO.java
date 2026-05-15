package com.spring.project.dto;

import com.spring.project.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Email
    private String email;
    @NotBlank
    @ValidPassword
    @ToString.Exclude
    private String password;
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    @NotNull
    @Positive
    private BigDecimal balance;
    private boolean blocked;
}
