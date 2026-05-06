package com.spring.project.dto;

import com.spring.project.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
public class EmployeeProfileForm {
    @NotBlank
    private String name;
    @NotNull
    private LocalDate birthDate;
    @NotBlank
    private String phone;
    @ValidPassword
    @ToString.Exclude
    private String password;
}
