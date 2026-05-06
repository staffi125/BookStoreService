package com.spring.project.dto;

import com.spring.project.model.enums.AgeGroup;
import com.spring.project.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String genre;
    @NotNull
    private AgeGroup ageGroup;
    @NotNull
    @Positive
    private BigDecimal price;
    @NotNull
    private LocalDate publicationDate;
    @NotBlank
    private String author;
    @NotNull
    @Positive
    private Integer pages;
    private String characteristics;
    private String description;
    @NotNull
    private Language language;
}
