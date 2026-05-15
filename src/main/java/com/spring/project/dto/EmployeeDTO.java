package com.spring.project.dto;

import com.spring.project.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @ValidPassword
    private String password;
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    @NotNull
    private LocalDate birthDate;
    @NotBlank
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$")
    private String phone;
}
