package com.spring.project.dto;

import com.spring.project.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientProfileForm {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    @ValidPassword
    private String password;
}
