package com.spring.project.dto;

import com.spring.project.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientProfileForm {
    @NotBlank
    private String name;
    @ValidPassword
    private String password;
}
